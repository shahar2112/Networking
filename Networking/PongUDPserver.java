package il.co.ilrd.Networking;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

/* The server must run before client, the server loops for 10
 * times, each iteration it receives a new "ping" msg from client,
 * after 10 iteration the server finishes */
public class PongUDPserver 
{
	public static void main(String[] args)
	{
		if (args.length != 1) 
		{
			System.err.println ("wrong arguments !!!");
			return;
		}
		
		// Selecting the arguments.
		String serverPortAsString = args[0];
		

		// Parsing the server port from string to int.
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
			System.err.println ("creating and binding server socket...");
			socket = new DatagramSocket(serverPort);
		} catch (SocketException exception)
		{
			// The socket can not be created or bound. (Maybe the port is already in use.)
			exception.printStackTrace (System.err);
			return;
		}
		
		//create an empty datagram in order to receive message request from client
		// Set a maximum request datagram backing buffer size.
		int requestDatagramBufferSize = 2048;
		
		// Create the request datagram backing buffer.
		byte[] requestDatagramBuffer = new byte[requestDatagramBufferSize];
		int requestDatagramBufferOffset = 0;
		
		// Create the request datagram. (We can reuse the same datagram for all requests.)
		DatagramPacket requestDatagram = 
		new DatagramPacket (requestDatagramBuffer, requestDatagramBufferOffset, requestDatagramBufferSize);
		
		// Set a maximum message counter.
		int counter = 10;
		
		// Wait for new datagrams.
		while (counter > 0)
		{
			// Receiving the request datagram.
			try
			{
				socket.receive (requestDatagram);
			} catch (IOException exception)
			{
				// An unknown / unexpected network error was encountered. (Not a normal situation.)
				exception.printStackTrace (System.err);
				return;
			}		
		
		
		// Obtaining the request datagram source address. (Which should be an instance of InetSocketAddress.)
		InetSocketAddress requestDatagramSource = (InetSocketAddress) requestDatagram.getSocketAddress ();
		
		// Obtaining the datagram actual length (how much data was received, or how much of the backing buffer was used).
		int requestDatagramBufferUsed = requestDatagram.getLength ();
		
		// Decoding the message (which should be a string).
		String requestMessage = new String (requestDatagramBuffer, requestDatagramBufferOffset, requestDatagramBufferUsed);
		
		//printing the message from client
		System.err.println ("received -->");
		System.out.println("    request message = " + requestMessage);
		
	
		// ----------
		// This is the pong sending part.
		// ----------
		
		// Creating the reply message.
		String replyMessage = "pong";
		
		// Creating the reply datagram backing buffer.
		byte[] replyDatagramBuffer = replyMessage.getBytes ();
		int replyDatagramBufferOffset = 0;
		int replyDatagramBufferSize = replyDatagramBuffer.length;
		
		// Setting the reply datagram destination address.
		InetSocketAddress replyDatagramDestination = requestDatagramSource;
		
		// Creating the reply datagram.
		DatagramPacket replyDatagram;
		replyDatagram = 
		new DatagramPacket (replyDatagramBuffer, replyDatagramBufferOffset, replyDatagramBufferSize, replyDatagramDestination);
		
		
		// Sending the reply datagram.
		try 
		{
			System.err.println ("sending reply datagram...");
			socket.send (replyDatagram);
		} catch (IOException exception) {
			// The datagram could not be sent. (Maybe destination is unreachable.)
			exception.printStackTrace (System.err);
		}
		counter--;
		}
		
		// Closing the socket.
		System.err.println ("closing socket...");
		socket.close ();
	}
	
}
