package com.joyl.iotserver;

import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.net.NetSocket;

public class SANode {
	static enum NodeStatus { ACTIVATED, WAITING, BLOCKED};
	
	// these variables are set during creation.
	private String name;
	private String ID;
	private String activationCode;
	
	// these variables are set by manager.
	private NodeStatus status;
	
	private JsonObject jsonObj;
	
	private NetSocket socket;

	public NetSocket getSocket() {
		return socket;
	}

	public void setSocket(NetSocket socket) {
		this.socket = socket;
	}

	SANode(String nodeJsonStr, NetSocket socket) {
		jsonObj = new JsonObject(nodeJsonStr);
		
		name = jsonObj.getString("nodeName");
		ID = jsonObj.getString("nodeID");
		activationCode = jsonObj.getString("activationCode");
		
		// TODO : Search DB whether this Device is activated before or not.
		status = NodeStatus.WAITING;
	}
	
	public JsonObject getDescriptionJsonObj() {
		JsonObject descJsonObj = new JsonObject();
		
		descJsonObj.putString("nodeID", ID);
		
		JsonObject sensorListObj = jsonObj.getObject("sensorList");
		
		if (sensorListObj != null) {
			descJsonObj.putObject("sensorList", sensorListObj);
		}
		
		JsonObject actuatorListObj = jsonObj.getObject("actuatorList");
		
		if (actuatorListObj != null) {
			descJsonObj.putObject("actuatorList", actuatorListObj);
		}
		
		return descJsonObj;
	}

	public NodeStatus getStatus() {
		return status;
	}

	public void setStatus(NodeStatus status) {
		this.status = status;
	}

	public String getName() {
		return name;
	}

	public String getID() {
		return ID;
	}

	public String getActivationCode() {
		return activationCode;
	}
	
	public boolean haveSensor() {
		if (jsonObj.getObject("sensorList") == null)
			return false;
		
		return true;
	}
	
	public boolean haveSensor(String sensorName) {
		if (jsonObj.getObject("sensorList") == null)
			return false;
		
//		System.out.println("haveSensor" + jsonObj.getObject("sensorList").encode());
		
		if (jsonObj.getObject("sensorList").getObject(sensorName) == null)
			return false;
		
		return true;
	}

	public boolean haveActuator() {
		if (jsonObj.getObject("actuatorList") == null)
			return false;
		
		return true;
	}

	public boolean haveActuator(String actuatorName) {
		if (jsonObj.getObject("actuatorList") == null)
			return false;
		
		System.out.println("haveActuator" + jsonObj.getObject("actuatorList").encode());
		
		if (jsonObj.getObject("actuatorList").getField(actuatorName) == null)
			return false;

		return true;
	}
	
	public JsonObject getFirstSensorValue() {
		JsonObject sensorList = jsonObj.getObject("sensorList");
		
		if (sensorList == null)
			return null;
		
		JsonObject sensorValue = new JsonObject();

		for (String sensorName : sensorList.getFieldNames()) {
			sensorValue.putString(sensorName, "0");
		}
		
		System.out.println("getFirstSensorValue" + sensorValue.encode());

		return sensorValue;
	}

	public JsonObject getFirstActuatorValue() {
		JsonObject actuatorList = jsonObj.getObject("actuatorList");
		
		if (actuatorList == null)
			return null;
		
		JsonObject actuatorValue = new JsonObject();

		for (String actuatorName : actuatorList.getFieldNames()) {
			actuatorValue.putString(actuatorName, actuatorList.getArray(actuatorName).get(0).toString());
		}
		
		System.out.println("getFirstActuatorValue" + actuatorValue.encode());
		
		return actuatorValue;
	}
}

