package com.joyl.iotserver;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.VertxFactory;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.net.NetSocket;
import org.vertx.java.core.parsetools.RecordParser;


public class ServerMain {
	static final String VERSION = "1.0";
	static final String ACTIVATIONCODE = "HBKK5Q7";
	static String password = null;
		
	static ControllerManager controllerManager = new ControllerManager();
	
	static SANodeManager nodeManager = new SANodeManager();
	static ConcurrentHashMap<String, NetSocket> nodeSocketMap = new ConcurrentHashMap<String, NetSocket>(); // SANodeID : NetSocket
	
	static ConcurrentHashMap<NetSocket, RequestToNode> requestToNodeMap = new ConcurrentHashMap<NetSocket, RequestToNode>();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		ServerMain serverMain = new ServerMain();

		RouteMatcher rm = new RouteMatcher();
		initializeRouteMatcher(rm);

		System.out.println("started");
		Vertx vertx = VertxFactory.newVertx("localhost");
		vertx.createHttpServer().requestHandler(rm)
//			.setSSL(true).setKeyStorePath("server-keystore.jks").setKeyStorePassword("wibble")
			.listen(50001);

		vertx.createNetServer().connectHandler(new Handler<NetSocket>() {
			public void handle(final NetSocket socket) {
		        socket.dataHandler(RecordParser.newDelimited("\n", new Handler<Buffer>() {
//			socket.dataHandler(new Handler<Buffer>() {
					public void handle(Buffer buffer) {
						// handling request from SANode
						handleNodeRequest(socket, buffer);
					}
				}));
				
				socket.closeHandler(new Handler<Void>() {
					public void handle(Void v) {
						// handling socket close
						for (String nodeID : nodeSocketMap.keySet()) {
							if (nodeSocketMap.equals(socket)) {
								nodeSocketMap.remove(nodeID);
								break;
							}
						}
						if (requestToNodeMap.get(socket) != null) {
							requestToNodeMap.remove(socket);
						}
					}
				});
			}
		}).listen(50000);
		
		long timeoutDuration = 72*60*60*1000;	// 72 hours
		new PeriodicTimer(timeoutDuration, timeoutDuration, timeoutDuration,
				controllerManager, nodeManager); 
	}

	static private void initializeRouteMatcher(RouteMatcher rm) {

		// IC00. Connection
		rm.get("/" + VERSION + "/", new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				if (password == null) {
					respondToController(req, 200, "{ \"next\" : \"activation\" }");
				}else {
					respondToController(req, 200, "{ \"next\" : \"login\" }");
				}
			}
		});

		// IC01. Activate Server
		rm.put("/" + VERSION + "/activation", new Handler<HttpServerRequest>() {
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
		rm.put("/" + VERSION + "/password", new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				printRequest(req);
				
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
		rm.put("/" + VERSION + "/login", new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				try {
					String controllerID = req.params().get("controllerID");
					String password = req.params().get("password"); 
					
					if (password == null || ServerMain.password == null || !ServerMain.password.equals(password)) {
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
		rm.get("/" + VERSION + "/nodelist", new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				try {
					String sessionID = req.params().get("sessionID");
					
					if (sessionID == null || !controllerManager.validateSession(sessionID)) {
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
		rm.put("/" + VERSION + "/nodelist/connected/:nodeid", new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				try {
					String nodeID = req.params().get("nodeid");
					String sessionID = req.params().get("sessionID");
					String activationCode = req.params().get("activationCode");
					
					if (sessionID == null || !controllerManager.validateSession(sessionID)) {
						throw new ControllerException(401);
					}
					if (activationCode == null) {
						throw new ControllerException(400);
					}
					
					if (!nodeManager.moveNodeToConnectedList(nodeID, activationCode)) {
						throw new ControllerException(403);
					}
							
					respondToController(req, 200);
					
					// TODO send to SANode that it is activated.
					sendToNode(nodeID, "/1.0/activation", "{ \"sessionID\" : \"" + nodeManager.getSessionID(nodeID) + "\" }", req, false);
				} catch (ControllerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					respondToController(req, e.errorCode, e.getMessage());
				}
			}
		});

		// IC06. Get connected SA Node List
		rm.get("/" + VERSION + "/nodelist/connected", new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				try {
					String sessionID = req.params().get("sessionID");
					
					if (sessionID == null || !controllerManager.validateSession(sessionID)) {
						throw new ControllerException(401);
					}
					// get connected List
					String connectedListStr = nodeManager.getConnectedListJsonStr();
					
					String nodeListStr =  "{ ";
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

		// IC07. Get waiting to be activated SA Node List
		rm.get("/" + VERSION + "/nodelist/waiting", new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				try {
					String sessionID = req.params().get("sessionID");
					
					if (sessionID == null || !controllerManager.validateSession(sessionID)) {
						throw new ControllerException(401);
					}
					// get waiting List
					String waitingListStr = nodeManager.getWaitingListJsonStr();
					
					String nodeListStr =  "{ ";
					if (waitingListStr != null) {
						nodeListStr += waitingListStr;
					}
					nodeListStr += "}";
							
					respondToController(req, 200, nodeListStr);
				} catch (ControllerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					respondToController(req, e.errorCode, e.getMessage());
				}
			}
		});

		// TODO : /nodelist/black*
		// IC08. Get SA Node black list 
		// IC09. Delete SA Node from black list
		// IC10. Put SA Node to black list
		
		// IC11. Get SA Node Info (Current value)
		// get current SA Node Info
		rm.get("/" + VERSION + "/nodelist/connected/:nodeid", new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				try {
					String sessionID = req.params().get("sessionID");
					
					if (sessionID == null || !controllerManager.validateSession(sessionID)) {
						throw new ControllerException(401);
					}
					// get waiting List
					String nodeInfoStr = nodeManager.getNodeValueJsonStr(req.params().get("nodeid"));
					
					respondToController(req, 200, nodeInfoStr);
				} catch (ControllerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					respondToController(req, e.errorCode, e.getMessage());
				}
			}
		});

		// IC12. Handle SA Node
		rm.put("/" + VERSION + "/nodelist/connected/:nodeid/:actuator", new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				try {
					String sessionID = req.params().get("sessionID");
					
					if (sessionID == null || !controllerManager.validateSession(sessionID)) {
						throw new ControllerException(401);
					}
					
					String nodeID = req.params().get("nodeid");
					
					// send to SANode
					if (!sendToNode(nodeID, "/1.0/actuator", 
							"{ \"" + req.params().get("actuator") + "\" : \"" + req.params().get("value") + "\" }", 
							req, false))
						throw new ControllerException(404);
//					respondToController(req, 200, nodeInfoStr);
				} catch (ControllerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					respondToController(req, e.errorCode, e.getMessage());
				}
				// initial connection
//				respondToController(req);
			}
		});

		// IC13. Get Log
		// get log
		rm.get("/" + VERSION + "/log", new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				try {
					String sessionID = req.params().get("sessionID");
					
					if (sessionID == null || !controllerManager.validateSession(sessionID)) {
						throw new ControllerException(401);
					}
					
					respondToController(req, 200, Logger.getLogList());
				} catch (ControllerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					respondToController(req, e.errorCode, e.getMessage());
				}
				// initial connection
//				respondToController(req);
			}
		});

		rm.get("/" + VERSION + "/log/:nodeid", new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				// initial connection
				respondToController(req);
			}
		});

		rm.get("/" + VERSION + "/log/:nodeid/:saname", new Handler<HttpServerRequest>() {
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
	
	static private void respondToController(HttpServerRequest req, int statusCode) {
//		printRequest(req);
//		
//		req.response().setStatusCode(statusCode);
//		req.response().headers().add("Content-Type", "json; charset=UTF-8");
//		req.response().end();
		respondToController(req, statusCode, "");
	}

	static private void respondToController(HttpServerRequest req, int statusCode, String jsonStr) {
		printRequest(req);
		
		req.response().setStatusCode(statusCode);
		req.response().headers().add("Content-Type", "json; charset=UTF-8");
		req.response().end(jsonStr);
		
		System.out.println(statusCode + " : " + jsonStr);
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
		// check whether request or respond
		String trimedBuffer = buffer.toString().trim();
		String[] urlNjson = trimedBuffer.split(" ");
		String jsonStr = (trimedBuffer.length() > urlNjson[0].length()) ? trimedBuffer.substring(urlNjson[0].length() + 1) : "";

		try {
			System.out.println(">> from Node : (" + urlNjson[0] + ") " + trimedBuffer);
		
		RequestToNode requestToNode = requestToNodeMap.get(socket);;
		
		switch (urlNjson[0]) // for URL
		{
		case "200" :
			if (requestToNode != null && requestToNode.isResponseNeeded) {
				switch (requestToNode.url) {
				case "/" + VERSION + "/activation" :
					respondToController(requestToNode.controllerReq, 200, requestToNode.param);
					break;
				case "/" + VERSION + "/actuator" :
					JsonObject jsonObj = new JsonObject(requestToNode.param);
					nodeManager.updateActuatorValue(requestToNode.nodeID, jsonObj);
					respondToController(requestToNode.controllerReq, 200, requestToNode.param);
					break;
				default :
					respondToController(requestToNode.controllerReq, 200);
					break;
				}
			}
			break;
		case "500" :
			if (requestToNode != null && requestToNode.isResponseNeeded) {
				respondToController(requestToNode.controllerReq, 500, jsonStr);
			}
			break;
		case "400" :
			if (requestToNode != null && requestToNode.isResponseNeeded) {
				respondToController(requestToNode.controllerReq, 500, jsonStr);
			}
			break;
		case "/" + VERSION + "/sanode" :	
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
			if (urlNjson[0].startsWith("/" + VERSION + "/sanode/")) {
				String[] splittedUrl = urlNjson[0].split("/");

				for (String s : splittedUrl) {
					System.out.println(">> echo : " + s);
				}
				
				JsonObject jsonObj = new JsonObject(jsonStr);

				String sessionID = jsonObj.getString("sessionID");
				String nodeID = splittedUrl[3];
				
				if (!nodeManager.isConnectedNode(nodeID)) 
					throw new NodeException(404);

				if (sessionID == null || !nodeManager.getSessionID(nodeID).equals(sessionID))
						throw new NodeException(401, "Invalid session ID.");
				
				// update socket info. 
				nodeSocketMap.put(nodeID, socket);

				switch (splittedUrl.length)
				{
				case 4 :
					// IS02. Send Sensor value
					//		/sanode/[nodeID]
					if (jsonObj.getObject("sensorList") != null)
						nodeManager.updateSensorValue(nodeID, sessionID, jsonObj.getObject("sensorList"));
					if (jsonObj.getObject("actuatorList") != null)
						nodeManager.updateActuatorValue(nodeID, sessionID, jsonObj.getObject("actuatorList"));
					
					socket.write("200\n");
		
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
			}else {
				throw new NodeException(400);
			}
			break;
		}
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			socket.write(e.getMessage() + "\n");
		}		
	}
	
	static boolean sendToNode(String nodeID, String url, String json, HttpServerRequest req, boolean isResponseNeeded) {
		NetSocket socket = nodeSocketMap.get(nodeID);
		
		if (socket == null)
			return false;
		
		requestToNodeMap.put(socket, new RequestToNode(nodeID, req, isResponseNeeded, url, json));
		nodeSocketMap.get(nodeID).write(url + " " + json + "\n");
		
		return true;
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

class RequestToNode {
	String nodeID;
	HttpServerRequest controllerReq;
	boolean isResponseNeeded;
	String url;
	String param;

//	public RequestToNode(String nodeID, HttpServerRequest controllerReq,
//			boolean isResponseNeeded) {
//		super();
//		this.nodeID = nodeID;
//		this.controllerReq = controllerReq;
//		this.isResponseNeeded = isResponseNeeded;
//	}

	public RequestToNode(String nodeID, HttpServerRequest controllerReq,
			boolean isResponseNeeded, String url, String param) {
		super();
		this.nodeID = nodeID;
		this.controllerReq = controllerReq;
		this.isResponseNeeded = isResponseNeeded;
		this.url = url;
		this.param = param;
	}
}

