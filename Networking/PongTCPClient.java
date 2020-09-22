package il.co.ilrd.Networking;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class PongTCPClient {
	

			 private static final int BUFFER_SIZE = 1024;
			 private static String[] messages = {"Pong", "pong", "pong", "exit"};

			  public static void main(String[] args) 
			  {

			    System.out.println("Starting MySelectorClientExample...");
		
			    try {
		
			      int port = 9999;
			      
			      //get host number 127.0.0.1
			      InetAddress hostIP = InetAddress.getLocalHost();
			      
			      //create the socket destination address
			      InetSocketAddress serverAdress = new InetSocketAddress(hostIP, port);
		
			      //creating a new SocketChannel to send data
			      SocketChannel myClient = SocketChannel.open(serverAdress);
			      
			      for (String msg: messages) 
			      {
			    	   //create buffer to send client msg
	    	  	        ByteBuffer myBuffer = ByteBuffer.allocate(BUFFER_SIZE);
	    	  	        
	    	  	        //add msg to buffer
	    	  	        myBuffer.put(msg.getBytes());
	    	  	        
	    	  	      // flip the mode of the byteBuffer (read -> write) and write to it the msg
	    	  	        myBuffer.flip();
	    	  	        myClient.write(myBuffer);
	    	  	        Thread.sleep(10000);	    	  	    
			      }
			      myClient.close();
				} catch (IOException | InterruptedException e)
			    {
					e.printStackTrace();
				}
			  }

}
