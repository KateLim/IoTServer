package com.joyl.discoveryserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class ControllerDiscoveryThread extends Thread {
	public static final String IOTSVR_MCAST_ADDRESS = "224.0.0.30";
	public static final int IOTSVR_MCAST_PORT = 50000;
	public static final int ANNOUNCE_INTERVAL = 1000;	// 1 second

	@Override
	public void run() {
		// TODO Auto-generated method stub
		InetAddress mcAddress = null; /* multicast address */
		int mcPort = 0; /* multicast port */
		int ttl = 1; /* time to live */
		byte[] sendBytes; /* bytes to be sent */
		boolean done = false;

		/* validate the multicast address argument */
		try {
			mcAddress = InetAddress.getByName(IOTSVR_MCAST_ADDRESS);
		} catch (UnknownHostException e) {
			System.err.println(IOTSVR_MCAST_ADDRESS + " is not a valid IP address");
			System.exit(1);
		}

		/* validate address argument is a multicast IP */
		if (!mcAddress.isMulticastAddress()) {
			System.err.println(mcAddress.getHostAddress()
					+ " is not a multicast IP address.");
			System.exit(1);
		}

		/* parse and validate port argument */
		try {
			mcPort = IOTSVR_MCAST_PORT;
		} catch (NumberFormatException nfe) {
			System.err.println("Invalid port number (" + IOTSVR_MCAST_PORT
					+ ")");
			System.exit(1);
		}

		try {

			/* instantiate a MulticastSocket */
			MulticastSocket sock = new MulticastSocket();

			/* set the time to live */
			sock.setTimeToLive(ttl); // Java 1.0/1.1 use setTTL()

			/* prepare to read from the keyboard input */
			// stdin=new BufferedReader(new InputStreamReader(System.in));

			System.out.println("Begin announcing");

			while (!done) {

				/* convert keyboard input to bytes */
				sendBytes = new String("50001").getBytes();

				/* populate the DatagramPacket */
				DatagramPacket packet = new DatagramPacket(sendBytes,
						sendBytes.length, mcAddress, mcPort);

				/* send the packet (announcing Port for controller) in every ANNOUNCE_INTERVAL seconds */
				sock.send(packet);
				Thread.sleep(ANNOUNCE_INTERVAL);
			}
			
			sock.close();

		} catch (IOException e) {
			System.err.println(e.toString());
			System.exit(1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			System.err.println(e.toString());
			System.exit(1);
			e.printStackTrace();
		}
	}

}
