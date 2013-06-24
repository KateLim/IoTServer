package com.joyl.iotserver;

public class SANodeSession {
	private SANode node;
	private String sessionID;
	private long lastHeartbeat;	// time in milliseconds
	private String lastSensorValue;

	public SANodeSession(SANode node, String sessionID) {
		// TODO Auto-generated constructor stub
		this.node = node;
		this.sessionID = sessionID;
		lastHeartbeat = System.currentTimeMillis();
	}
	
	public String getSessionID() {
		return sessionID;
	}

	public SANode getNode() {
		return node;
	}

	public void setLastHeartbeat(long lastHeartbeat) {
		this.lastHeartbeat = lastHeartbeat;
	}
	
	public void setLastSensorValue(String lastSensorValue) {
		this.lastSensorValue = lastSensorValue;
	}

	public boolean isValidSession(String sessionID) {
		if (sessionID.equals(this.sessionID))
			return true;
		
		return false;
	}
	
	public boolean isAlive(long sessionTimeout) {
		if (sessionID == null || lastHeartbeat == 0)
			return false;
		
		if ( (System.currentTimeMillis() - lastHeartbeat) > sessionTimeout)
			return false;
		
		return true;
	}
}
