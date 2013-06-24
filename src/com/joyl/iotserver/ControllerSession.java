package com.joyl.iotserver;

public class ControllerSession {
	String sessionID;
	String controllerID;
	private long lastHeartbeat;	// time in milliseconds

	public ControllerSession(String controllerID, String sessionID) {
		// TODO Auto-generated constructor stub
		this.controllerID = controllerID;
		this.sessionID = sessionID;
		lastHeartbeat = System.currentTimeMillis();
	}

	public boolean isAlive(long sessionTimeout) {
		if (sessionID == null || lastHeartbeat == 0)
			return false;
		
		if ( (System.currentTimeMillis() - lastHeartbeat) > sessionTimeout)
			return false;
		
		return true;
	}
	
	public void updateSession() {
		lastHeartbeat = System.currentTimeMillis();
	}
}
