package com.joyl.iotserver;

import java.util.HashMap;

public class ControllerManager {
	// TODO This is temporal implementation. need to improve to REAL session ID
	private static final String SESSIONIDBASE = "coJlwhi12ofA";
	private static int sessionIdx = 0;

	private HashMap<String, ControllerSession> controllerList = new HashMap<String, ControllerSession>();	// Activated & Connected Node List

	public ControllerManager() {
		// TODO Auto-generated constructor stub
	}
	
	private String generateSessionID() {
		String sessionID =  SESSIONIDBASE + sessionIdx++;
		
		return sessionID;
	}
	
	public String addNewSession(String controllerID) {
		String sessionID = generateSessionID();
		
		controllerList.put(sessionID, new ControllerSession(controllerID, sessionID));
		return sessionID;
	}
	
	public boolean validateSession(String sessionID) {
		if (!controllerList.containsKey(sessionID))
		return false;
		
		controllerList.get(sessionID).updateSession();
		return true;
	}

	public void removeOldSession(long timeDurationMillis) {
		for (String sessionID : controllerList.keySet()) {
			if (!controllerList.get(sessionID).isAlive(timeDurationMillis))
				controllerList.remove(sessionID);			
		}
	}
}
