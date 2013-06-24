package com.joyl.iotserver;

import org.vertx.java.core.json.JsonObject;

public class SANode {
	static enum NodeStatus { ACTIVATED, WAITING, BLOCKED};
	
	// these variables are set during creation.
	private String name;
	private String ID;
	private String activationCode;
	
	// these variables are set by manager.
	private NodeStatus status;
	
	private JsonObject jsonObj;

	SANode(String nodeJsonStr) {
		jsonObj = new JsonObject(nodeJsonStr);
		
		name = jsonObj.getString("nodeName");
		ID = jsonObj.getString("nodeID");
		activationCode = jsonObj.getString("activationCode");
		
		// TODO : Search DB whether this Device is activated before or not.
		status = NodeStatus.WAITING;
	}
	
	public String getDescriptionJsonStr() {
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
		
		return descJsonObj.toString();
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
}

