package com.joyl.iotserver;

import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.net.NetSocket;

public class SANodeSession  extends Session {
	private SANode node;
	private JsonObject lastSensorValue;	// sensor value in Json String
	private JsonObject lastActuatorValue;	// actuator value in Json String

	public SANodeSession(SANode node, String sessionID) {
		// TODO Auto-generated constructor stub
		this.node = node;
		this.sessionID = sessionID;
		lastHeartbeat = System.currentTimeMillis();

		if (this.node.haveSensor()) {
			lastSensorValue = node.getFirstSensorValue();
		} else 
			lastSensorValue = null;
		
		if (this.node.haveActuator()) {
			lastActuatorValue = node.getFirstActuatorValue();
		} else 
			lastActuatorValue = null;
	}
	
	public SANode getNode() {
		return node;
	}

	public void setLastHeartbeat(long lastHeartbeat) {
		this.lastHeartbeat = lastHeartbeat;
	}
	
	public void setLastSensorValue(JsonObject lastSensorValue) {
		
		if (this.lastSensorValue == null)
			return;
		
		for (String sensorName : lastSensorValue.getFieldNames()) {
			if (node.haveSensor(sensorName))
				// TODO temporarily change to get Number instead of string
				this.lastSensorValue.putString(sensorName, lastSensorValue.getNumber(sensorName).toString());
//				this.lastSensorValue.putString(sensorName, lastSensorValue.getString(sensorName));
		}
		
		Logger.logSANodeValue(node.getID(), node.getName(), getLastSensorActuatorValueStr());
	}

	public void setLastActuatorValue(JsonObject lastActuatorValue) {
		if (this.lastActuatorValue == null)
			return;
		
		for (String actuatorName : lastActuatorValue.getFieldNames()) {
			if (node.haveActuator(actuatorName))
				this.lastActuatorValue.putString(actuatorName, lastActuatorValue.getString(actuatorName));
		}

		Logger.logSANodeValue(node.getID(), node.getName(), getLastSensorActuatorValueStr());
	}

	public String getLastSensorActuatorValueStr() {
		JsonObject jsonObj = new JsonObject();
		
		if (lastSensorValue != null)
			jsonObj.putObject("sensorList", lastSensorValue);

		if (lastActuatorValue != null)
			jsonObj.putObject("actuatorList", lastActuatorValue);
		
		return jsonObj.toString();
	}

}
