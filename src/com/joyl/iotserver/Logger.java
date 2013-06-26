package com.joyl.iotserver;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

public class Logger {

//	static LinkedHashMap<Long, String> logList = new LinkedHashMap<Long, String>();
	static DBCollection collLog;

	public static void setCollLog(DBCollection collLog) {
		Logger.collLog = collLog;
	}

	public Logger() {
		// TODO Auto-generated constructor stub
	}

	private static DBObject jsonToDBObject(JsonObject object) {
		String str = object.encode();
		return (DBObject) JSON.parse(str);
	}

	public static void logSANodeValue(String nodeID, String nodeName,
			String nodeValue) {
		JsonObject jsonObj = new JsonObject();
		Long timestamp = System.currentTimeMillis();

		jsonObj.putNumber("time", timestamp);
		jsonObj.putString("type", "SANode");
		jsonObj.putString("nodeID", nodeID);
		jsonObj.putString("nodeName", nodeName);
		jsonObj.putObject("data", new JsonObject(nodeValue));

		collLog.insert(jsonToDBObject(jsonObj));
	}

	public static void logControllerCommand(String request, int statusCode, String respond) {
		JsonObject jsonObj = new JsonObject();
		Long timestamp = System.currentTimeMillis();

		jsonObj.putNumber("time", timestamp);
		jsonObj.putString("type", "Controller");
		jsonObj.putObject("request", new JsonObject(request));

		JsonObject resJsonObj = new JsonObject();
		resJsonObj.putString("code", "" + statusCode);
		if (respond != null && respond.length() != 0) {
			if (respond.length() > 1024)
				resJsonObj.putString("data", "deprecated because too long");
			else
				resJsonObj.putObject("data", new JsonObject(respond));
		}
		
		jsonObj.putObject("resonse", resJsonObj);
		collLog.insert(jsonToDBObject(jsonObj));
	}

	public static void removeOldLog(long timeDurationMillis) {
		long deadlineMillis = System.currentTimeMillis() - timeDurationMillis;

		if (collLog == null)
			return;

//		System.out.println(deadlineMillis);
		collLog.remove((DBObject) JSON.parse("{ time : {$lt : " + deadlineMillis + "}}"));

	}

	public static String getLogList() {
		DBCursor cursor = collLog.find(null, (DBObject) JSON.parse("{ _id : 0, nodeID : 0 }")).sort(
				(DBObject) JSON.parse("{ time : -1 }"));

		JsonArray logArray = new JsonArray();

		try {
			while (cursor.hasNext()) {
				String data = cursor.next().toString();
				JsonObject jsonObj = new JsonObject(data);
				System.out.println(data);
				logArray.addObject(jsonObj);
			}
		} finally {
			cursor.close();
		}

		JsonObject logList = new JsonObject();
		logList.putArray("log", logArray);

		return logList.encode();
	}
}
