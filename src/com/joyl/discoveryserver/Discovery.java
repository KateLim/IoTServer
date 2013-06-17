package com.joyl.discoveryserver;

import java.net.*; /* import networking package */
import java.util.ArrayList;
import java.io.*; /* import input/output package */

public class Discovery {
	public static final int MAX_ND_THREAD_CNT = 1;//024;

	public static void main(String argv[]) {
		ControllerDiscoveryThread cdThread = new ControllerDiscoveryThread();
		ArrayList<NodeDiscoveryThread> ndThreadList = new ArrayList<NodeDiscoveryThread>();
		
		cdThread.start();
		
		IPList ipList = new IPList();
		ipList.makeIpAddrList();
		
//		int maxSize = (ipList.size() > MAX_ND_THREAD_CNT) ? MAX_ND_THREAD_CNT : ipList.size();
//		
//		for (int i=0 ; i < maxSize ; i++) {
//			ndThreadList.add(new NodeDiscoveryThread(ipList));
//		}
//		
//		for (NodeDiscoveryThread ndThread : ndThreadList) {
//			ndThread.start();
//		}
	}
}
