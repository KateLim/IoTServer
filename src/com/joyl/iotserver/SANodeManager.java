package com.joyl.iotserver;

import java.util.ArrayList;
import java.util.HashMap;

import com.joyl.iotserver.SANode.NodeStatus;

public class SANodeManager {
	// TODO This is temporal implementation. need to improve to REAL session ID
	private static final String SESSIONIDBASE = "agJlwhi12ofA";
	private static int sessionNum = 0;
	
	private HashMap<String, SANodeSession> connectedNodeList = new HashMap<String, SANodeSession>();	// Activated & Connected Node List
	private HashMap<String, SANode> waitingNodeList = new HashMap<String, SANode>();	// Not activated but connected Node List
	
	private ArrayList<String> activatedIDList = new ArrayList<String>();	// Activated ID List from DB
	private ArrayList<String> blockedIDList = new ArrayList<String>();	// Blocked ID List from DB
	
	public SANodeManager() {
		// TODO Auto-generated constructor stub
		// TODO Read activated ID List and blocked ID List from DB
		activatedIDList.add("abcd");
		blockedIDList.add("efgh");
	}

	private boolean IsInActivatedList(String nodeID) {
		if (activatedIDList.contains(nodeID))
			return true;
		return false;
	}

	private boolean IsInBlockedList(String nodeID) {
		if (blockedIDList.contains(nodeID))
			return true;
		return false;
	}
	
	private String generateSessionID() {
		String sessionID =  SESSIONIDBASE + sessionNum++;
		
		return sessionID;
	}
	
	public NodeStatus handleNewNode(SANode node) {
		if (IsInActivatedList(node.getID())) {
			node.setStatus(NodeStatus.ACTIVATED);
			SANodeSession nodeSession = new SANodeSession(node, generateSessionID());
			connectedNodeList.put(node.getID(), nodeSession);
		}else if (IsInBlockedList(node.getID())) {
			node.setStatus(NodeStatus.BLOCKED);
		}else {
			node.setStatus(NodeStatus.WAITING);
			waitingNodeList.put(node.getID(), node);
		}
		
		return node.getStatus();
	}
	
	public boolean updateHeartbeat(String nodeID, String sessionID) {
		SANodeSession session = connectedNodeList.get(nodeID);
		
		if (session == null || session.isValidSession(sessionID) == false)
			return false;

		session.setLastHeartbeat(System.currentTimeMillis());
		return true;
	}
	
	public boolean updateSensorValue(String nodeID, String sessionID, String sensorValue) {
		SANodeSession session = connectedNodeList.get(nodeID);

		if (session == null || session.isValidSession(sessionID) == false)
			return false;
	
		session.setLastSensorValue(sensorValue);
		
		return true;
	}
	
	public String getSessionID(String nodeID) {
		SANodeSession session = connectedNodeList.get(nodeID);

		if (session == null)
			return null;
		
		return session.getSessionID();
	}
	
	public String getWaitingListJsonStr() {
		if (waitingNodeList.isEmpty())
			return null;
		
		String nodeListJsonStr = "";
		boolean first=true;
		
		nodeListJsonStr = "\"waitingList\" : {";
		for (String nodeID : waitingNodeList.keySet()) {
			if (first)
				first = false;
			else
				nodeListJsonStr += ", ";
			
			SANode node = waitingNodeList.get(nodeID);
			nodeListJsonStr += "\"" + node.getName() + "\" : " + node.getDescriptionJsonStr();
		}
		nodeListJsonStr += "}";
		
		return nodeListJsonStr;
	}
	
	public String getConnectedListJsonStr() {
		if (connectedNodeList.isEmpty())
			return null;
		
		String nodeListJsonStr = "";
		boolean first=true;
		
		nodeListJsonStr = "\"connectedList\" : {";
		for (String nodeID : connectedNodeList.keySet()) {
			if (first)
				first = false;
			else
				nodeListJsonStr += ", ";
			
			SANode node = connectedNodeList.get(nodeID).getNode();
			nodeListJsonStr += "\"" + node.getName() + "\" : " + node.getDescriptionJsonStr();
		}
		nodeListJsonStr += "}";
		
		return nodeListJsonStr;
	}
}
