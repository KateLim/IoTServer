package com.joyl.iotserver;

import java.util.LinkedHashMap;

import org.vertx.java.core.json.JsonObject;

public class Logger {

	static LinkedHashMap<Long, String> logList = new LinkedHashMap<Long, String>();
	
	public Logger() {
		// TODO Auto-generated constructor stub
	}
	
	// { time : milliseconds, type : "
	public static void logSANodeValue(String nodeID, String nodeName, String nodeValue) {
		JsonObject jsonObj = new JsonObject();
		Long timestamp = System.currentTimeMillis();
		jsonObj.putString("time", timestamp.toString());
		jsonObj.putString("type", "SANode");
		jsonObj.putString("nodeID", nodeID);
		jsonObj.putString("nodeName", nodeName);
		jsonObj.putObject("data", new JsonObject(nodeValue));
		logList.put(timestamp, jsonObj.toString());
	}

	public static void logControllerCommand(String nodeID, String nodeName, String controllerID, String command) {
		JsonObject jsonObj = new JsonObject();
		Long timestamp = System.currentTimeMillis();
		jsonObj.putString("time", timestamp.toString());
		jsonObj.putString("type", "Controller");
		jsonObj.putString("nodeID", nodeID);
		jsonObj.putString("nodeName", nodeName);
		jsonObj.putString("controllerID", controllerID);
		jsonObj.putObject("data", new JsonObject(command));
		logList.put(timestamp, jsonObj.toString());
	}
	
	public static void removeOldLog(long timeDurationMillis) {
		long deadlineMillis = System.currentTimeMillis() - timeDurationMillis;

		for (Long timestamp : logList.keySet()) {
			if (timestamp < deadlineMillis)
				logList.remove(timestamp);
			else
				break;
		}
	}
	
	public static String getLogList() {
		String logListStr = "{ \"log\" : [";
		boolean bFirst = true;
		
		for (Long timestamp : logList.keySet()) {
			if (bFirst) {
				bFirst = false;
			}else {
				logListStr += ",";
			}
			logListStr += logList.get(timestamp);
		}
		logListStr += "]}";
		
		return logListStr;		
	}
}
