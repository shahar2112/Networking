package il.co.ilrd.Networking;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

/* The client must run after server, the server loops for 10
 * times, each iteration it receives a new "ping" msg from client,
 * after 10 iteration the server finishes */
public class PingUDPclient 
{
	public static void main(String[] args)
	{
		if (args.length != 2) 
		{
			System.out.println(args.length);
			System.err.println ("wrong arguments !!!");
			return;
		}
		
		// Selecting the arguments.
		String serverIPAsString = args[0];
		String serverPortAsString = args[1];
		
		// Resolving the server address changing string to Inetaddress.
		InetAddress serverIP;
		try 
		{
			serverIP = InetAddress.getByName (serverIPAsString);
		} catch (UnknownHostException exception)
		{
			System.err.println ("wrong server name !!!");
			return;
		}
		
		// Parsing the server port, changing string to int.
		int serverPort;
		try 
		{
			serverPort = Integer.parseInt (serverPortAsString);
		} catch (NumberFormatException exception) 
		{
			System.err.println ("wrong server port !!!");
			return;
		}
		
		// Creating and binding the socket, using datagram for UDP.
		DatagramSocket socket;
		try 
		{
			System.err.println ("creating and binding client socket...");
			socket = new DatagramSocket();
		}
		catch (SocketException exception)
		{
			exception.printStackTrace (System.err);
			return;
		}


		// Creating the datagram destination (that is the server socket address).
		InetSocketAddress datagramDestination = new InetSocketAddress (serverIP, serverPort);
		
		// Creating the ping message.
		String message = "ping";

		// Creating the datagram buffer.
		byte[] datagramBuffer = message.getBytes();
		int datagramBufferOffset = 0;
		int datagramBufferSize = datagramBuffer.length;
		
		// Creating the datagram.
		DatagramPacket datagram;
		
		//the datagram with details we send and where to send
		datagram = new DatagramPacket (datagramBuffer, datagramBufferOffset, datagramBufferSize, datagramDestination);
		
		// Sending the datagram.
		try 
		{
			socket.send (datagram);
		} catch (IOException exception) 
		{
			// The datagram could not be sent. (Maybe destination is unreachable.)
			exception.printStackTrace (System.err);
			socket.close ();
			return;
		}
		
		
		// ----------
		// This is the pong reply receiving.
		// ----------
		
		//prepering an empty datagrampacket to receive the servers reply
		// Set a maximum datagram backing buffer size.
		datagramBufferSize = 2048;
		
		// Create the datagram backing buffer.
		datagramBuffer = new byte[datagramBufferSize];
		datagramBufferOffset = 0;
		
		// Re-create the datagram.
		datagram = new DatagramPacket (datagramBuffer, datagramBufferOffset, datagramBufferSize);
		
		// Receiving the datagram.
		try 
		{
			socket.receive (datagram);
		} catch (IOException exception) 
		{
			// An unknown / unexpected network error was encountered. (Not a normal situation.)
			exception.printStackTrace (System.err);
			socket.close ();
			return;
		}
		
		// Obtaining the datagram actual length (how much data was received, or how much of the backing buffer was used).
		int datagramBufferUsed = datagram.getLength();
		
		// Decoding the message (which should be a string).
		message = new String (datagramBuffer, datagramBufferOffset, datagramBufferUsed);
		
		//printing message received from client
		System.err.println ("received -->");
		System.out.println ("    message = " + message);
		
		// ----------
		
		// Closing the socket.
		System.err.println ("closing socket...");
		socket.close ();
			
	}

}

