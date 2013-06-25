package com.joyl.iotserver;

public class ControllerSession extends Session {
	String controllerID;
	
	public ControllerSession(String controllerID, String sessionID) {
		// TODO Auto-generated constructor stub
		this.controllerID = controllerID;
		this.sessionID = sessionID;
		lastHeartbeat = System.currentTimeMillis();
	}
}
