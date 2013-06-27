package com.joyl.iotserver;

import java.util.ArrayList;
import java.util.HashMap;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import com.joyl.iotserver.SANode.NodeStatus;

public class SANodeManager {
	// TODO This is temporal implementation. need to improve to REAL session ID
	private static final String SESSIONIDBASE = "agJlwhi12ofA";
	private static int sessionNum = 0;
	
	private HashMap<String, SANodeSession> connectedNodeList = new HashMap<String, SANodeSession>();	// Activated & Connected Node List
	private HashMap<String, SANode> waitingNodeList = new HashMap<String, SANode>();	// Not activated but connected Node List
	
	public SANodeManager() {
	}

	private boolean isInActivatedList(String nodeID) {
		// TODO must check whether user registered this id no not
		return ConfigManager.isNodeRegistered(nodeID);
//		return activatedIDList.contains(nodeID);
	}

	private String generateSessionID() {
		String sessionID =  SESSIONIDBASE + sessionNum++;
		
		return sessionID;
	}
	
	public boolean isConnectedNode(String nodeID) {
		return connectedNodeList.containsKey(nodeID);
	}
	
	public NodeStatus handleNewNode(SANode node) {
		if (isInActivatedList(node.getID())) {
			node.setStatus(NodeStatus.ACTIVATED);
//			SANodeSession nodeSession = new SANodeSession(node, generateSessionID());
			// IMPORTANT session ID is not automatically generated but use Activation Code for temporal implementation
			SANodeSession nodeSession = new SANodeSession(node, node.getActivationCode());
			connectedNodeList.put(node.getID(), nodeSession);
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
	
	public boolean updateSensorValue(String nodeID, String sessionID, JsonObject sensorValue) {
		SANodeSession session = connectedNodeList.get(nodeID);

		if (session == null || session.isValidSession(sessionID) == false)
			return false;
	
		session.setLastSensorValue(sensorValue);
		
		return true;
	}
	
	public boolean updateActuatorValue(String nodeID, String sessionID, JsonObject actuatorValue) {
		SANodeSession session = connectedNodeList.get(nodeID);

		if (session == null || session.isValidSession(sessionID) == false)
			return false;
	
		session.setLastActuatorValue(actuatorValue);
		
		return true;
	}

	// when controller asks to set actuator value (we can't know session ID)
	public boolean updateActuatorValue(String nodeID, JsonObject actuatorValue) {
		SANodeSession session = connectedNodeList.get(nodeID);

		session.setLastActuatorValue(actuatorValue);
		
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
			nodeListJsonStr += "\"" + node.getName() + "\" : " + node.getDescriptionJsonObj().encode();
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
			nodeListJsonStr += "\"" + node.getName() + "\" : " + node.getDescriptionJsonObj().encode();
		}
		nodeListJsonStr += "}";
		
		return nodeListJsonStr;
	}
	
	public ArrayList<String> getConnectedList() {
		ArrayList<String> connectedIDList = new ArrayList<String>();
		
		for (String nodeID : connectedNodeList.keySet()) {
			connectedIDList.add(nodeID);
		}
		
		return connectedIDList;
	}

	/**
	 * @param nodeID
	 * @return true if node is successfully moved to connected list or if node is already in connected list.
	 */
	public boolean moveNodeToConnectedList(String nodeID) {
		SANode node = waitingNodeList.get(nodeID);
		if (node == null) {
			if (connectedNodeList.containsKey(nodeID))
				return true;
			return false;
		}
		
		System.out.println("++++MOVE TO CONNECTED LIST++++++" + node.getDescriptionJsonObj());
		node.setStatus(NodeStatus.ACTIVATED);
//		connectedNodeList.put(nodeID, new SANodeSession(node, generateSessionID()));
		// IMPORTANT session ID is not automatically generated but use Activation Code for temporal implementation
		connectedNodeList.put(nodeID, new SANodeSession(node, node.getActivationCode()));
				
		waitingNodeList.remove(nodeID);
		
		return true;
	}
	
	/**
	 * @param nodeID
	 * @return true if node is successfully moved to waiting list or if node is already in waiting list.
	 */
	public boolean moveNodeToWaitingList(String nodeID) {
		if (waitingNodeList.containsKey(nodeID)) {
			return true;
		}
		if (!connectedNodeList.containsKey(nodeID))
			return false;

		SANode node = connectedNodeList.get(nodeID).getNode();
		waitingNodeList.put(nodeID, node);
		connectedNodeList.remove(nodeID);
		
		System.out.println("++++MOVE TO WAITING LIST++++++" + node.getDescriptionJsonObj());
		node.setStatus(NodeStatus.WAITING);

		return true;
	}

	public String getNodeValueJsonStr(String nodeID) {
		SANodeSession session = connectedNodeList.get(nodeID);
		
		if (session == null)
			return null;
		
		return session.getLastSensorActuatorValueStr();
	}

	public void removeOldSession(long timeDurationMillis) {
		for (String nodeID : connectedNodeList.keySet()) {
			if (!connectedNodeList.get(nodeID).isAlive(timeDurationMillis))
				connectedNodeList.remove(nodeID);			
		}
	}
	
	public String getNodeListStrbyUserID(String userID, boolean generateWaitingList, boolean generateConnectedList) {
		ArrayList<String> registeredNodeIDList = ConfigManager.getRegisteredNodeIDList(userID);

		JsonObject connectedListObj = new JsonObject();
		JsonArray waitingListArray = new JsonArray();
		
		for (String nodeID : registeredNodeIDList) {
			SANodeSession session = connectedNodeList.get(nodeID);
			
			if (session == null) {
				waitingListArray.add(nodeID);
			}else {
				connectedListObj.putObject(session.getNode().getName(), session.getNode().getDescriptionJsonObj());
			}
		}

		JsonObject nodeList = new JsonObject();

		if (generateWaitingList)
			nodeList.putArray("waitingList", waitingListArray);
		
		if (generateConnectedList)
			nodeList.putObject("connectedList", connectedListObj);
		
		return nodeList.encode();
	}
}
