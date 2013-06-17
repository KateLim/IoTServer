package com.joyl.discoveryserver;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

public class IPList {
	ArrayList<String> ipAddrList;
	int ipCurIndex;
	long lastTimeMillis;
	
	IPList() 
	{
		ipCurIndex = 0;
		lastTimeMillis = 0;
		ipAddrList = new ArrayList<String>();
	}
	
	public synchronized String getNextIpAddr()
	{
		String currIpAddr = ipAddrList.get(ipCurIndex);
		
		if (ipCurIndex == 0)
		{
			long curTimeMillis = System.currentTimeMillis();
			
			if (lastTimeMillis == 0)
				System.out.println("First IP Address. Time : " + curTimeMillis);
			else 
				System.out.println("Wrapped to First IP Address. Elapsed Time : " + (curTimeMillis - lastTimeMillis));
			
			lastTimeMillis = curTimeMillis;
		}
		
		ipCurIndex++;
		if (ipCurIndex == ipAddrList.size())
		{
			ipCurIndex = 0;
		}
			
		return currIpAddr;
	}
	
	public void makeIpAddrList()
	{
//		int searchSize = 2048;							// The number of IP address to scan at the same time.
//		int	portNum = 500;								// Port number for server socket
//		int i = 0;										// Loop index
	  	int oct1start, oct1finish;						// The first IP octet range (starting/ending IP addresses)
	  	int oct2start, oct2finish;						// The second IP octet range (starting/ending IP addresses)
	  	int oct3start, oct3finish;						// The third IP octet range (starting/ending IP addresses)
	  	int oct4start, oct4finish;						// The forth IP octet range (starting/ending IP addresses)
	  	int one, two, three, four;						// The four octets. These are used as loop indexes
	  	int octet;										// A single octet
	   	String myIpAddr = null;							// This application's IP address
	   	String mySubnetMask = null;						// The subnet mask this application is running on
	   	String searchIpAddr = null;						// The IP address to scan
//	   	Thread tcList[] = new TryConnect[searchSize];	// The list of threads that actually does the scan
//	   	boolean FirstAddr = true;						// Flag indicating the first IP address

		/***********************************************************************************************************
		* We get our IP address and our Subnet mask and print it to the terminal
		***********************************************************************************************************/
		try {
//			InetAddress localHost = Inet4Address.getLocalHost();
			Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
			
			System.out.println(nets);
        	for (NetworkInterface networkInterface : Collections.list(nets)) {
				System.out.println("====================");
				for (InterfaceAddress address : networkInterface.getInterfaceAddresses()) {
					InetAddress inetAddress = address.getAddress();
					
					// for getting IPV4 format
      				if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address) ) {

	                    myIpAddr = inetAddress.getHostAddress().toString();
	                    mySubnetMask = getIPv4LocalNetMask(inetAddress, address.getNetworkPrefixLength());
	                    //System.out.println("ip---::" + ip);
	                    break;
                    }
   					//System.out.println("Address: " + address);
	    			//System.out.println(" prefixLength : " + address.getNetworkPrefixLength());
				}
				
				if (myIpAddr != null)
					break;
            }
//			InetAddress localHost = Inet4Address.getLocalHost();
//			NetworkInterface networkInterface = NetworkInterface.getByInetAddress(localHost);
//			short prefixLength = networkInterface.getInterfaceAddresses().get(0).getNetworkPrefixLength();
			
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	   	//myIpAddr = GetMyIp();
	   	System.out.println( "My IP Address:: " + myIpAddr );

	   	//mySubnetMask = GetSubnetMask();
	   	System.out.println( "Subnet Mask:: " + mySubnetMask +"\n\n" );

		/***********************************************************************************************************
		* Next we get the integer equivalent of each octet. We do this to determine the scan ranges.
		* This is the first Octet...
		***********************************************************************************************************/

	   	octet = GetOctet( 1, mySubnetMask);

	   	switch( octet )
	   	{
		   	case 255:									// No Addresses
		   		oct1start = GetOctet( 1, myIpAddr );
	  			oct1finish = oct1start + 1;
	  			break;

	  		case 0:										// All 255 Addresses
	  			oct1start = 0;
	  			oct1finish = 255;
	  			break;

	  		default:									// Partial mask
				oct1start = 0;
				oct1finish = octet;
		}

		/***********************************************************************************************************
		* This is the second Octet...
		***********************************************************************************************************/

	   	octet = GetOctet( 2, mySubnetMask);

	   	switch( octet )
	   	{
		   	case 255:									// No Addresses
		   		oct2start = GetOctet( 2, myIpAddr );
	  			oct2finish = oct2start + 1;
	  			break;

	  		case 0:										// All 255 Addresses
	  			oct2start = 0;
	  			oct2finish = 255;
	  			break;

	  		default:									// Partial mask
				oct2start = 0;
				oct2finish = octet;
		}

		/***********************************************************************************************************
		* This is the third Octet...
		***********************************************************************************************************/

		octet = GetOctet( 3, mySubnetMask);

	   	switch( octet )
	   	{
		   	case 255:									// No Addresses
		   		oct3start = GetOctet( 3, myIpAddr );
	  			oct3finish = oct3start + 1;
	  			break;

	  		case 0:										// All 255 Addresses
	  			oct3start = 0;
	  			oct3finish = 255;
	  			break;

	  		default:									// Partial mask
				oct3start = 0;
				oct3finish = octet;
		}

		/***********************************************************************************************************
		* This is the fourth Octet...
		***********************************************************************************************************/

		octet = GetOctet( 4, mySubnetMask);

	   	switch( octet )
	   	{
		   	case 255:									// No Addresses
		   		oct4start = GetOctet( 4, myIpAddr );
	  			oct4finish = oct4start + 1;
	  			break;

	  		case 0:										// All 255 Addresses
	  			oct4start = 0;
	  			oct4finish = 255;
	  			break;

	  		default:									// Partial mask
				oct4start = 0;
				oct4finish = octet;
		}

		/***********************************************************************************************************
		* Now we start the scan. This algorithm will scan about 2K addresses per minute.
		***********************************************************************************************************/

		System.out.println( "Making IP Address list...\n\n" );

		for ( one = oct1start; one < oct1finish; one++ )						// First octet
		{
			for ( two = oct2start; two < oct2finish; two++ )					// Second octet
			{
	  			for ( three=oct3start; three < oct3finish; three++ )			// Third octet
	   			{
		   			for ( four=oct4start; four<oct4finish; four++ )				// Fourth octet
		   			{
			   			searchIpAddr = new String( String.valueOf(one));		// Here we build the IP string
			   			searchIpAddr += ".";
			   			searchIpAddr += String.valueOf(two);
			   			searchIpAddr += ".";
			   			searchIpAddr += String.valueOf(three);
			   			searchIpAddr += ".";
			   			searchIpAddr += String.valueOf(four);

		/***********************************************************************************************************
		* Here we instantiate a TryConnect thread for each IP address. The scans happen simultaniously. We can do
		* upto searchSize at a time.
		***********************************************************************************************************/


						ipAddrList.add(searchIpAddr);
//			   			tcList[i] = new TryConnect(searchIpAddr, portNum);
//			   			tcList[i].start();

		/***********************************************************************************************************
		* What we are checking for here is to see if the tcList thread pool is full. If so, we wait about 3 seconds
		* for the threads to finish. Then we start again. We report the scan ranges to the users so they can see
		* the progress of the scan.
		***********************************************************************************************************/

//			   			if ( i == searchSize-1 )
//			   			{
//			   				System.out.println( " to " + searchIpAddr );
//			   				FirstAddr = true;
//			   				i = 0;
//			   				try
//			   				{
//			   					Thread.sleep(3000); //give time for the first batch of connects to finish
//
//		   					} catch ( Exception e ) {
//
//			   					System.out.println( "Error in sleep " + e );
//		   					}
//
//		   				} else {
//			   				if ( FirstAddr )
//			   				{
//				   				System.out.print( "Checking " + searchIpAddr );
//				   				FirstAddr = false;
//			   				}
//			   				i++;
//		   				}

					} // for
				} // for
			} // for
		} // for

		System.out.println( "Number of IP Address List : " + ipAddrList.size());

 	} // Main

	/*
	 * Get network mask for the IP address and network prefix specified...
	 * The network mask will be returned has an IP, thus you can
	 * print it out with .getHostAddress()...
	 */
	String getIPv4LocalNetMask(InetAddress ip, int netPrefix) {
	
	    try {
	        // Since this is for IPv4, it's 32 bits, so set the sign value of
	        // the int to "negative"...
	        int shiftby = (1<<31);
	        // For the number of bits of the prefix -1 (we already set the sign bit)
	        for (int i=netPrefix-1; i>0; i--) {
	            // Shift the sign right... Java makes the sign bit sticky on a shift...
	            // So no need to "set it back up"...
	            shiftby = (shiftby >> 1);
	        }
	        // Transform the resulting value in xxx.xxx.xxx.xxx format, like if
	        /// it was a standard address...
	        String maskString = Integer.toString((shiftby >> 24) & 255) + "." + Integer.toString((shiftby >> 16) & 255) + "." + Integer.toString((shiftby >> 8) & 255) + "." + Integer.toString(shiftby & 255);

	        return maskString;
	        // Return the address thus created...
//	        return InetAddress.getByName(maskString);
	    }
	        catch(Exception e){e.printStackTrace();
	    }
	    // Something went wrong here...
	    return null;
	}


	/***********************************************************************************************************
	* This method gets the specified Octet from an IP string. The octet is returned as an integer value.
	***********************************************************************************************************/

  	static int GetOctet( int OctetNum, String ipString )
 	{
	 	int Octet;				// The Octet
	 	String tempstring;		// Temporary string

		// Here we figure out which octet the caller wants, and then parse it from the ipString passed
		// into this method.

	 	switch( OctetNum )
	 	{
		 	case 1:
		 		Octet = Integer.valueOf(ipString.substring(0, ipString.indexOf(".")));
		 		break;

		 	case 2:
		 		tempstring = ipString.substring(ipString.indexOf(".")+1);
		 		Octet = Integer.valueOf(tempstring.substring(0, tempstring.indexOf(".")));
		 		break;

		 	case 3:
		 		tempstring = ipString.substring(ipString.indexOf(".")+1);
		 		tempstring = tempstring.substring(tempstring.indexOf(".")+1);
		 		Octet = Integer.valueOf(tempstring.substring(0, tempstring.indexOf(".")));
		 		break;

		 	case 4:
		 		tempstring = ipString.substring(ipString.indexOf(".")+1);
		 		tempstring = tempstring.substring(tempstring.indexOf(".")+1);
		 		tempstring = tempstring.substring(tempstring.indexOf(".")+1);
		 		Octet = Integer.valueOf(tempstring);
		 		break;

			default:
				Octet = -1;

		} // switch

		return( Octet );

	}// GetOctet

	public int size() {
		// TODO Auto-generated method stub
		return ipAddrList.size();
	}


} // class