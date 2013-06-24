package com.joyl.iotserver;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.VertxFactory;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.net.NetSocket;


public class ServerMain {
	static final String ACTIVATIONCODE = "HBKK5Q7";
	static String password = null;
		
	static ControllerManager controllerManager = new ControllerManager();
	
	static SANodeManager nodeManager = new SANodeManager();
	static HashMap<String, NetSocket> nodeSocketMap = new HashMap<String, NetSocket>(); // SANodeID : NetSocket

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		RouteMatcher rm = new RouteMatcher();
		initializeRouteMatcher(rm);

		System.out.println("started");
		Vertx vertx = VertxFactory.newVertx("localhost");
		vertx.createHttpServer().requestHandler(rm)
//			.setSSL(true).setKeyStorePath("server-keystore.jks").setKeyStorePassword("wibble")
			.listen(50001);

		vertx.createNetServer().connectHandler(new Handler<NetSocket>() {
			public void handle(final NetSocket socket) {
				socket.dataHandler(new Handler<Buffer>() {
					public void handle(Buffer buffer) {
						// handling request from SANode
						handleNodeRequest(socket, buffer);
					}
				});
			}
		}).listen(50000);
	}

	static private void initializeRouteMatcher(RouteMatcher rm) {

		// IC00. Connection
		rm.get("/", new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				if (password == null) {
					respondToController(req, 200, "{ \"next\" : \"activation\" }");
				}else {
					respondToController(req, 200, "{ \"next\" : \"login\" }");
				}
			}
		});

		// IC01. Activate Server
		rm.put("/activation", new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				try {
					String activationCode = req.params().get("activationCode"); 
							
					if (activationCode == null || !activationCode.equals(ACTIVATIONCODE)) {
						throw new ControllerException(400);
					}
					
					respondToController(req, 200, "{ \"next\" : \"password\" }");

				} catch (ControllerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					respondToController(req, e.errorCode, e.getMessage());
				}
			}
		});

		// IC02. Set password
		rm.put("/password", new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				try {
					String password = req.params().get("password"); 
					
					if (password == null || ServerMain.password != null) {
						throw new ControllerException(400);
					}
					
					ServerMain.password = password;
					respondToController(req, 200, "{ \"next\" : \"login\" }");

				} catch (ControllerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					respondToController(req, e.errorCode, e.getMessage());
				}
			}
		});

		// IC03. Log-in with password
		rm.put("/login", new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				try {
					String controllerID = req.params().get("controllerID");
					String password = req.params().get("password"); 
					
					if (password == null || !ServerMain.password.equals(password)) {
						throw new ControllerException(400);
					}
					
					String sessionID = controllerManager.addNewSession(controllerID);
					
					respondToController(req, 200, "{ \"sessionID\" : \"" + sessionID +"\" }");
	
				} catch (ControllerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					respondToController(req, e.errorCode, e.getMessage());
				}
			}
		});

		// IC04. Get SA node list (waiting list + connected list)
		rm.get("/nodelist", new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				try {
					String sessionID = req.params().get("sessionID");
					
					if (sessionID == null || !controllerManager.isValidSessionID(sessionID)) {
						throw new ControllerException(401);
					}
					// get waiting List
					// get activated List
					String waitingListStr = nodeManager.getWaitingListJsonStr();
					String connectedListStr = nodeManager.getConnectedListJsonStr();
					
					String nodeListStr =  "{ ";
					if (waitingListStr != null) {
						nodeListStr += waitingListStr;
						
						if (connectedListStr != null)
							nodeListStr += ",";
					}
					
					if (connectedListStr != null)
						nodeListStr += connectedListStr;
					
					nodeListStr += "}";
							
					respondToController(req, 200, nodeListStr);
				} catch (ControllerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					respondToController(req, e.errorCode, e.getMessage());
				}
			}
		});

		// IC05. Activate SA Node
		// handle SA Node : /nodelist/connected/[nodeID]/[actuatorName]
		rm.put("/nodelist/connected/:nodeid", new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				// initial connection
				respondToController(req);
			}
		});

		// IC06. Get connected SA Node List
		rm.get("/nodelist/connected", new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				// initial connection
				respondToController(req);
			}
		});

		// IC07. Get waiting to be activated SA Node List
		rm.get("/nodelist/waiting", new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				// initial connection
				respondToController(req);
			}
		});

		// TODO : /nodelist/black*
		// IC08. Get SA Node black list 
		// IC09. Delete SA Node from black list
		// IC10. Put SA Node to black list
		
		// IC11. Get SA Node Info (Current value)
		// get current SA Node Info
		rm.get("/nodelist/connected/:nodeid", new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				// initial connection
				respondToController(req);
			}
		});

		// IC12. Handle SA Node
		rm.put("/nodelist/connected/:nodeid/:actuator", new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				// initial connection
				respondToController(req);
			}
		});

		// IC13. Get Log
		// get log
		rm.get("/log", new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				// initial connection
				respondToController(req);
			}
		});

		rm.get("/log/:nodeid", new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				// initial connection
				respondToController(req);
			}
		});

		rm.get("/log/:nodeid/:saname", new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				// initial connection
				respondToController(req);
			}
		});

		// Catch all - serve the index page
		rm.getWithRegEx(".*", new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				req.response().sendFile("route_match/index.html");
			}
		});
	}

	static private void printRequest(HttpServerRequest req) {
		System.out.println("requestUri : "+ req.uri());
		System.out.println("remoteAddress : "+ req.remoteAddress());

		for (String query : req.params().names()) {
			System.out.println("\tparams : " + query + ":" + req.params().get(query));
		}
		System.out.println("\t----------------");
		for (String key : req.headers().names()) {
			System.out.println("\theaders : " + key + ":" + req.headers().get(key));
		}
	}
	
	static private void respondToController(HttpServerRequest req, int statusCode, String jsonStr) {
		printRequest(req);
		
		req.response().setStatusCode(statusCode);
		req.response().headers().add("Content-Type", "json; charset=UTF-8");
		req.response().end(jsonStr);
	}
	
	static private void respondToController(HttpServerRequest req) {
		JsonObject resJson = new JsonObject();
		JsonArray resParamsArr = new JsonArray();

		resJson.putString("requestUri", req.uri());

		for (String query : req.params().names()) {
			JsonObject param = new JsonObject();

			param.putString(query, req.params().get(query));
			resParamsArr.addObject(param);
		}
		resJson.putArray("requestParams", resParamsArr);

		respondToController(req, 200, resJson.toString());
		
//		System.out.println(resJson.toString());
//		System.out.println("Headers are: ");
//		for (String key : req.headers().names()) {
//			System.out.println(key + ":" + req.headers().get(key));
//		}
//		req.response().headers().add("Content-Type", "json; charset=UTF-8");
//		req.response().end(resJson.toString());
		// .end("{ \"request_uri\" : \"" + req.uri() + "\" ," +
		// " \"response\" : { \"test_array\" : [{ \"a\" : \"b\", \"c\" : \"d\" }, { \"e\" : \"f\"}]}}");
		// String file = req.path().equals("/") ? "index.html" :
		// req.path();
		// req.response().sendFile("webroot/" + file);
	}
	
	static void handleNodeRequest(final NetSocket socket, Buffer buffer) {
		String[] urlNjson = buffer.toString().split(" ");
		String jsonStr = buffer.toString().substring(urlNjson[0].length() + 1);

		try {
		
		if (!urlNjson[0].startsWith("/1.0/")) {
			throw new NodeException(400, "Invalid version");
		}
		
		for (String s : urlNjson) {
			System.out.println(">> echo : " + s);
		}
		
		switch (urlNjson[0]) // for URL
		{
		case "/1.0/sanode" :	
			// IS01. Connect to IoT Server
			//		/sanode
			SANode node = new SANode(jsonStr);
			
			switch (nodeManager.handleNewNode(node)) {
				case ACTIVATED :
					socket.write("200 { \"sessionID\" : \"" + nodeManager.getSessionID(node.getID()) + "\"}\n");
					nodeSocketMap.put(node.getID(), socket);
					break;
				case WAITING :
					nodeSocketMap.put(node.getID(), socket);
					throw new NodeException(401);
				case BLOCKED :
					throw new NodeException(403);
			}
			break;
		default :
			if (urlNjson[0].startsWith("/1.0/sanode/")) {
				String[] splittedUrl = urlNjson[0].split("/");

				for (String s : splittedUrl) {
					System.out.println(">> echo : " + s);
				}

				JsonObject jsonObj = new JsonObject(jsonStr);

				String sessionID = jsonObj.getString("sessionID");
				String nodeID = splittedUrl[3];
				
				if (sessionID == null)
						throw new NodeException(401, "Invalid session ID.");
				
				// update socket info. 
				nodeSocketMap.put(nodeID, socket);

					switch (splittedUrl.length)
				{
				case 4 :
					// IS02. Send Sensor value
					//		/sanode/[nodeID]
					if (nodeManager.updateSensorValue(nodeID, sessionID, jsonObj.getObject("sensorList").toString()))
						socket.write("200\n");
					else
						throw new NodeException(401, "Invalid session ID.");
		
					break;
				case 5 :
					// IS03. Send Heartbeat in every ?? seconds
					// 		/sanode/[nodeID]/heartbeat
					if (nodeManager.updateHeartbeat(nodeID, sessionID))
						socket.write("200\n");
					else
						throw new NodeException(401, "Invalid session ID.");
					break;
				default :
					// error!
					throw new NodeException(400);
				}
			}
			break;
		}
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			socket.write(e.getMessage() + "\n");
		}		
	}
}

class NodeException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4790342278288060378L;
	private static final Map<Integer, String> ERRORSTR;
	
	static {
		Map<Integer, String> errorStr = new HashMap<Integer, String>();
		errorStr.put(400, "Bad request");
		errorStr.put(401, "Not activated SA Node");
		errorStr.put(403, "SA Node is in blacklist");
		errorStr.put(500, "Internal server failure");

        ERRORSTR = Collections.unmodifiableMap(errorStr);
    }
	
	NodeException(int errorCode) {
		super(errorCode + " { \"errorMessage\" : \"" + ERRORSTR.get(errorCode) + "\"}");
	}

	NodeException(int errorCode, String errorString) {
		super(errorCode + " { \"errorMessage\" : \"" + ERRORSTR.get(errorCode) + " - " + errorString + "\"}");
	}
}

class ControllerException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3193429952825948103L;
	private static final Map<Integer, String> ERRORSTR;

	static {
		Map<Integer, String> errorStr = new HashMap<Integer, String>();
		errorStr.put(400, "Bad request");
		errorStr.put(401, "Invalid session ID");
		errorStr.put(403, "Activation code mismatch");
		errorStr.put(404, "Requested SA Node not found");
		errorStr.put(500, "Internal server failure");

        ERRORSTR = Collections.unmodifiableMap(errorStr);
    }
	
	public int errorCode;
	
	ControllerException(int errorCode) {
		super(" { \"errorMessage\" : \"" + ERRORSTR.get(errorCode) + "\"}");
		this.errorCode = errorCode;
	}

	ControllerException(int errorCode, String errorString) {
		super(" { \"errorMessage\" : \"" + ERRORSTR.get(errorCode) + " - " + errorString + "\"}");
		this.errorCode = errorCode;
	}
}
