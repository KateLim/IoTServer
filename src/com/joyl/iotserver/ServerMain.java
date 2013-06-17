package com.joyl.iotserver;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.VertxFactory;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.net.NetSocket;

public class ServerMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("started");
		Vertx vertx = VertxFactory.newVertx("localhost");
		vertx.createHttpServer()
				.requestHandler(new Handler<HttpServerRequest>() {
					public void handle(HttpServerRequest req) {
						System.out.println("Got request: " + req.uri());
						System.out.println("Headers are: ");
						for (String key : req.headers().names()) {
							System.out.println(key + ":"
									+ req.headers().get(key));
						}
						req.response()
								.headers()
								.add("Content-Type", "json; charset=UTF-8");
						req.response()
								.end("{ \"request_uri\" : \"" + req.uri() + "\" ," +
						                " \"response\" : { \"test_array\" : [{ \"a\" : \"b\", \"c\" : \"d\" }, { \"e\" : \"f\"}]}}");
						// String file = req.path().equals("/") ? "index.html" :
						// req.path();
						// req.response().sendFile("webroot/" + file);
					}
				}).listen(50001);

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

}
