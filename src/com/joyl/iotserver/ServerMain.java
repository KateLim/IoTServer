package com.joyl.iotserver;

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
						socket.write(buffer);
					
					}
				});
			}
		}).listen(50000);
	}

	static private void initializeRouteMatcher(RouteMatcher rm) {

		rm.get("/", new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				// initial connection
				responseTemplate(req);
			}
		});

		rm.put("/activation", new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				// initial connection
				responseTemplate(req);
			}
		});

		rm.put("/password", new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				// initial connection
				responseTemplate(req);
			}
		});

		rm.put("/login", new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				// initial connection
				responseTemplate(req);
			}
		});

		rm.get("/nodelist", new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				// initial connection
				responseTemplate(req);
			}
		});

		// activate SA Node : /nodelist/connected/[nodeID]/[actuatorName]
		// handle SA Node : /nodelist/connected/[nodeID]/[actuatorName]
		rm.put("/nodelist/connected/:nodeid", new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				// initial connection
				responseTemplate(req);
			}
		});

		rm.put("/nodelist/connected/:nodeid/:actuator", new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				// initial connection
				responseTemplate(req);
			}
		});

		// get current SA Node Info
		rm.getWithRegEx("/nodelist/connected/*", new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				// initial connection
				responseTemplate(req);
			}
		});

		rm.get("/nodelist/connected", new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				// initial connection
				responseTemplate(req);
			}
		});

		rm.get("/nodelist/waiting", new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				// initial connection
				responseTemplate(req);
			}
		});

		// TODO : /nodelist/black*
		
		// get log
		rm.get("/log", new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				// initial connection
				responseTemplate(req);
			}
		});

		rm.get("/log/:nodeid", new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				// initial connection
				responseTemplate(req);
			}
		});

		rm.get("/log/:nodeid/:saname", new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				// initial connection
				responseTemplate(req);
			}
		});

		// Catch all - serve the index page
		rm.getWithRegEx(".*", new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				req.response().sendFile("route_match/index.html");
			}
		});
	}

	static private void responseTemplate(HttpServerRequest req) {
		JsonObject resJson = new JsonObject();
		JsonArray resParamsArr = new JsonArray();

		resJson.putString("requestUri", req.uri());

		for (String query : req.params().names()) {
			JsonObject param = new JsonObject();

			param.putString(query, req.params().get(query));
			resParamsArr.addObject(param);
		}
		resJson.putArray("requestParams", resParamsArr);
		System.out.println(resJson.toString());
		System.out.println("Headers are: ");
		for (String key : req.headers().names()) {
			System.out.println(key + ":" + req.headers().get(key));
		}
		req.response().headers().add("Content-Type", "json; charset=UTF-8");
		req.response().end(resJson.toString());
		// .end("{ \"request_uri\" : \"" + req.uri() + "\" ," +
		// " \"response\" : { \"test_array\" : [{ \"a\" : \"b\", \"c\" : \"d\" }, { \"e\" : \"f\"}]}}");
		// String file = req.path().equals("/") ? "index.html" :
		// req.path();
		// req.response().sendFile("webroot/" + file);
	}
}
