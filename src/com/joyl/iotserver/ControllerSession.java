package com.joyl.iotserver;

public class ControllerSession extends Session {
	String userID;

	public ControllerSession(String userID, String sessionID) {
		// TODO Auto-generated constructor stub
		this.userID = userID;
		this.sessionID = sessionID;
		lastHeartbeat = System.currentTimeMillis();
	}

	public String getUserID() {
		return userID;
	}

}
