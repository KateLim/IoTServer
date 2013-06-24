package com.joyl.iotserver;

import java.util.HashMap;

public class ControllerManager {
	// TODO This is temporal implementation. need to improve to REAL session ID
	private static final String SESSIONIDBASE = "coJlwhi12ofA";
	private static int sessionNum = 0;

	private HashMap<String, ControllerSession> controllerList = new HashMap<String, ControllerSession>();	// Activated & Connected Node List

	public ControllerManager() {
		// TODO Auto-generated constructor stub
	}
	
	private String generateSessionID() {
		String sessionID =  SESSIONIDBASE + sessionNum++;
		
		return sessionID;
	}
	
	public String addNewSession(String controllerID) {
		String sessionID = generateSessionID();
		
		controllerList.put(sessionID, new ControllerSession(controllerID, sessionID));
		return sessionID;
	}
	
	public boolean isValidSessionID(String sessionID) {
		if (controllerList.get(sessionID) == null)
			return false;
		
		return true;
	}
}
