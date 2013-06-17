package com.joyl.discoveryserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class NodeDiscoveryThread extends Thread {

	public static final int SANODE_PORT = 50000;
	IPList ipList;

	public NodeDiscoveryThread(IPList ipList) {
		// TODO Auto-generated constructor stub
		this.ipList = ipList;
	}

	@Override
	public void run() {
		boolean done = false;
		
		// TODO Auto-generated method stub
		while (!done) {
			connect(ipList.getNextIpAddr(), SANODE_PORT);
		}
	}
	
	public void connect(String ipAddress, int port)
	{
		try {
			/*****************************************************************************
			 * First we try to connect. We set the timeout to 3 1/4 seconds.
			 * This value is critcal. If its too short, its not enough time
			 * to connect, but the longer it is, the longer it take to scan
			 * the addresses.
			 *****************************************************************************/

			SocketAddress sockaddr = new InetSocketAddress(
					ipAddress, port);
			Socket sock = new Socket();
			System.out.println("\n\nTrying connect to :: " + sockaddr.toString());
			sock.connect(sockaddr, 3250);
			System.out.println("SERVER FOUND AT:: " + sockaddr.toString()
					+ "!!!");

			/*****************************************************************************
			 * If we get here, we are connected. Now we determine if is an
			 * Arduino server running the CommandServer application. So
			 * wecreate the input and output streams. The "out" variable is
			 * for writing to the server, and "in" is for reading from the
			 * server.
			 *****************************************************************************/

			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
			BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));

			/*****************************************************************************
			 * The protocol is simple: The client sends a message, then
			 * waits for two messages from the server and thats it. We send
			 * a query command the client which is a "Q\n" string. If the
			 * write timesout, we will drop down to the exception which
			 * means that while we have found a server, its not an Arduino
			 * running the CommandServer application.
			 *****************************************************************************/

			System.out.println(sock.getLocalAddress().toString() + ":" + 50002);
			out.write(sock.getLocalAddress().toString() + ":" + 50002 + "\n");
			out.flush();

			in.close();
			out.close();
			sock.close();

		} catch (IOException e) {

			// System.out.println( "NO SERVER FOUND AT::" + ip );
			// System.exit(1);
		}
	}
}
