package com.joyl.iotserver;

import java.util.ArrayList;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;

public class ConfigManager {
	static DBCollection collCfg;

	public static void setCollCfg(DBCollection collCfg) {
		ConfigManager.collCfg = collCfg;
	}

	public ConfigManager() {
		// TODO Auto-generated constructor stub
	}

	public static boolean addAccount(String id, String password) {
		DBObject obj = collCfg.findOne((DBObject) JSON.parse("{ ID : \"" + id
				+ "\" }"));

		// if ID already exists, return false
		if (obj != null)
			return false;

		collCfg.insert((DBObject) JSON.parse("{ ID : \"" + id
				+ "\", password : \"" + password + "\" }"));

		return true;
	}

	public static boolean checkAccountIDPassword(String id, String password) {
		DBObject obj = collCfg.findOne((DBObject) JSON.parse("{ ID : \"" + id
				+ "\" }"));

		// if ID doesn't exist, return false
		if (obj == null)
			return false;

		// if password matches, return true
		JsonObject jsonObj = new JsonObject(obj.toString());
		if (password.equals(jsonObj.getString("password")))
			return true;

		return false;
	}

	public static boolean addNodeToAccount(String id, String nodeID) {
		// db.config.update({ ID : "mimir"}, {$addToSet : { nodes : "kkkk"}})
		WriteResult res = collCfg.update(
				(DBObject) JSON.parse("{ ID : \"" + id + "\" }"),
				(DBObject) JSON.parse("{ $addToSet : { nodes : \"" + nodeID
						+ "\" }}"));

		if (res.getError() == null) {
			return false;
		} else {
			return true;
		}
	}

	public static boolean removeNodeFromAccount(String id, String nodeID) {
		// db.config.update({ ID : "mimir"}, {$pull : { nodes : "kkkk"}})
		WriteResult res = collCfg.update(
				(DBObject) JSON.parse("{ ID : \"" + id + "\" }"),
				(DBObject) JSON.parse("{ $pull : { nodes : \"" + nodeID
						+ "\" }}"));

		if (res.getError() == null) {
			return false;
		} else {
			return true;
		}
	}

	public static ArrayList<String> getMatchedNodeID(String id,
			ArrayList<String> connectedNodeIDList) {
		DBObject obj = collCfg.findOne((DBObject) JSON.parse("{ ID : \"" + id
				+ "\" }"));

		// if ID doesn't exist, return false
		if (obj == null)
			return null;

		JsonObject jsonObj = new JsonObject(obj.toString());
		JsonArray jsonArr = jsonObj.getArray("nodes");

		ArrayList<String> matchedList = new ArrayList<String>();

		for (String nodeID : connectedNodeIDList) {
			if (jsonArr.contains(nodeID))
				matchedList.add(nodeID);
		}

		return null;
	}

	public static ArrayList<String> getRegisteredNodeIDList(String id) {
		DBObject obj = collCfg.findOne((DBObject) JSON.parse("{ ID : \"" + id
				+ "\" }"));
		ArrayList<String> registeredList = new ArrayList<String>();

		// if ID doesn't exist, return false
		if (obj == null)
			return registeredList;

		JsonObject jsonObj = new JsonObject(obj.toString());
		JsonArray jsonArr = jsonObj.getArray("nodes");

		if (jsonArr == null)
			return registeredList;

		for (Object curObj : jsonArr) {
			registeredList.add(curObj.toString());
		}

		System.out.println("getRegisteredNodeIDList : "
				+ registeredList.toString());
		return registeredList;
	}

	public static boolean isNodeRegistered(String nodeID) {
		DBObject obj = collCfg.findOne((DBObject) JSON.parse("{ nodes : \""
				+ nodeID + "\" }"));

		if (obj == null)
			return false;

		return true;
	}

	public static boolean isNodeRegistered(String nodeID, String id) {
		DBObject obj = collCfg.findOne((DBObject) JSON.parse("{ ID : \"" + id
				+ "\", nodes : \"" + nodeID + "\" }"));

		if (obj == null)
			return false;

		return true;
	}

	public static JsonArray getRegisteredUserIDList(String nodeID) {
		DBCursor cursor = collCfg.find(
				(DBObject) JSON.parse("{ nodes : \"" + nodeID + "\" }"),
				(DBObject) JSON.parse("{ _id : 0, nodes : 0 }"));

		JsonArray idArray = new JsonArray();

		try {
			while (cursor.hasNext()) {
				String data = cursor.next().toString();
				JsonObject jsonObj = new JsonObject(data);
				System.out.println(data);
				idArray.addString(jsonObj.getString("ID"));
			}
		} finally {
			cursor.close();
		}

		return idArray;
	}
}
