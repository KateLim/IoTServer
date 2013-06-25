package com.joyl.iotserver;

public class Session {

	protected String sessionID;
	protected long lastHeartbeat;

	public Session() {
		super();
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

	public String getSessionID() {
		return sessionID;
	}

	public boolean isValidSession(String sessionID) {
		if (sessionID.equals(this.sessionID))
			return true;
		
		return false;
	}
}