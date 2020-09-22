package il.co.ilrd.Networking;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class PingPongTCPServer {

	private static final int BUFFER_SIZE = 1024;
	private static Selector selector = null;
	
	public static void main(String[] args)
	{
		try {
		
				// get host number 127.0.0.1 and port number
			   InetAddress hostIP = InetAddress.getLocalHost();
			   int port = 9999;
			   
			   //Opening a selector
			   selector = Selector.open();
			   
			   //Opening a socket channel, creating a socket
			   ServerSocketChannel mySocket = ServerSocketChannel.open();
			   
			   //creating the self server socket destination
			   InetSocketAddress address = new InetSocketAddress(hostIP, port);
			   
			   //binds the ServerSocket to the specified socket address
			   mySocket.socket().bind(address);
			   
			   //setting the socket to non-blocking mode
			   mySocket.configureBlocking(false);
			   
			   //the serversocketchannel support only accepting new connections operations
			   //the int returned from validops will represent the selection key of accept
			   int ops = mySocket.validOps();
	
			   mySocket.register(selector, ops, null);
			   
			   while (true) 
				{
					// the select method blocked, until a registered channel/s is/are ready for an operation.
				    selector.select();
				    // store in a set the relevant selected keys. each key represent a ready channel
				    Set<SelectionKey> selectedKeys = selector.selectedKeys();
				    Iterator<SelectionKey> i = selectedKeys.iterator();
				    
				    // iterate through the keys, determine what is the key's event
				    while (i.hasNext()) 
				    {
				    	SelectionKey key = i.next();
				    	if (key.isAcceptable()) 
				    	{
				    		processAcceptEvent(mySocket);
				    	} 
				    	else if (key.isReadable()) 
				    	{
				    		processReadEvent(key);
				    	}
				    	i.remove();
				    }
				}
				
			} catch (IOException exception) {
				exception.printStackTrace (System.err);
				return;
			}
		}
		
		private static void processAcceptEvent(ServerSocketChannel mySocket) throws IOException 
		{
			System.out.println("Connection Accepted...");
			
			// Accept the connection and make it non-blocking
			SocketChannel myClient = mySocket.accept();
			
			myClient.configureBlocking(false);
			
			// Register interest in reading this channel
			myClient.register(selector, SelectionKey.OP_READ);
		}
		
		 
		
		 private static void processReadEvent(SelectionKey key) throws IOException 
		 {
		  //The channel() method returns the SelectableChannel object associated with the key
		  // create a SocketChannel to read the request
		  SocketChannel myClient = (SocketChannel) key.channel();
	
		  // Set up out 1k buffer to read data into
		  ByteBuffer myBuffer = ByteBuffer.allocate(BUFFER_SIZE);
	
		  //reads from channel to my buffer
		  myClient.read(myBuffer);
	
		 //trim method removes spaces from the beginning and end of string
		  String data = new String(myBuffer.array()).trim();
	
			  if (data.length() > 0)
			  {
				  System.out.println(String.format("Message Received.....: %s\n", data));
			  
				   if (data.equalsIgnoreCase("exit"))
				   {
					   myClient.close();
					   System.out.println("Closing Server Connection...");
			
				   }
			  }
		 }
	 
}
