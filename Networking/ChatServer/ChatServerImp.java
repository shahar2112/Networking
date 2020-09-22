package il.co.ilrd.Networking.ChatServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import il.co.ilrd.HashMap.HashMap;

public class ChatServerImp implements ChatServer
{
	private static final int BUFFER_SIZE = 1024;
	private HashMap<String,List<SocketChannel>> groupMap;
	private Selector selector;
	boolean toStop;
	int port;
	InetAddress hostIP;

	
	public ChatServerImp(int potentialGroups)
	{
		groupMap = new HashMap<>(potentialGroups);
		toStop = false;
		port = 9999;
		try {
			hostIP = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {e.printStackTrace();}

	}
	
	
	@Override
	public void startServer() 
	{
		//creating a serverThread, so the main thread (from main method) won't block
		new Thread( new ServerThread()).start();;
	}
	
	private class ServerThread implements Runnable
	{
		
		private void selectorLoop(ServerSocketChannel mySocketChannel)
		{
			while (!toStop) 
			{
				// the select method blocked, until a registered channel/s is/are ready for an operation.
			    try {
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
				    		processAcceptEvent(mySocketChannel);
				    	} 
				    	else if (key.isReadable()) 
				    	{
				    		processReadEvent(key);
				    	}
				    	i.remove();
				    } 
				    } catch (IOException e) { e.printStackTrace();}
			}
		}
		
		@Override
		public void run() 
		{
			try
			{
				selector = Selector.open();
				
				//set the localhost number 127.0.0.1 as hostIP
				InetAddress hostIP = InetAddress.getLocalHost();
							
				// Creating a self "server socket address" (object of server IP + server port).
				InetSocketAddress socketaddress = new InetSocketAddress(hostIP, port);
				
		 		// creating the server socket channel
				ServerSocketChannel mySocketChannel = ServerSocketChannel.open();
				
				// create a socket to the channel and bind it to the adress (ip + port)
				mySocketChannel.bind(socketaddress);
				
				// making out channel non-blocking.
				mySocketChannel.configureBlocking(false);
				
				// the serversocketchannel support only accepting new connections operation.
				// the int returned from ValidOps method will represent the  SelectionKey.OP_ACCEPT
				int ops = mySocketChannel.validOps();
				mySocketChannel.register(selector, ops, null);	
				
				selectorLoop(mySocketChannel);
			}
			catch (IOException exception) {
				exception.printStackTrace (System.err);
				return;
			}
		}
	}

	@Override
	public void stopServer() 
	{
		try {
			toStop = true;
			//creating a new socketChannel in order to arise an accept event to the selector,
			//so the while loop in selectorLoop will meet the condition of toStop
			SocketChannel.open(new InetSocketAddress(hostIP, port));
			
			for (String group : groupMap.keySet())
			{
				for (SocketChannel clientChannel : groupMap.get(group))
				{
					removeClient(group, clientChannel);
				}		
			}
			
			selector.close();
		} catch (IOException e) {e.printStackTrace();}
	}
	
	
	
	private void processAcceptEvent(ServerSocketChannel mySocketChannel) throws IOException 
	{
		
		// Accept the connection and make it non-blocking
		SocketChannel myClient = mySocketChannel.accept();
		myClient.configureBlocking(false);
		
		// Register interest in reading this channel
		myClient.register(selector, SelectionKey.OP_READ);
	}	
	
	private void processReadEvent(SelectionKey key) throws IOException 
	{		
		// the channel method returns a selectableChannel associated with the key 
		SocketChannel myClient = (SocketChannel) key.channel();
		// Set up 1k buffer to read data into
		ByteBuffer myBuffer = ByteBuffer.allocate(BUFFER_SIZE);
		
		// reads from channel to myBuffer
		myClient.read(myBuffer);
		
		//processing the read message
		String Fullmessege = new String(myBuffer.array());
		
		String[] split = Fullmessege.split("@",3);
		String group = split[0];
		String name = split[1];
		String messege = split[2].trim();
	
		if (askedForRegister(messege))
		{
			registerClient(group, name, myClient);
			return;
		}
		
		if (askedForRemovel(messege))
		{
			removeClient(group, myClient);
		}

		broadcast(group, name, messege, myClient);			
	}
	
	private boolean askedForRegister(String clientMessege)
	{
		return clientMessege.trim().equals("##register##");
	}
	
	private void registerClient(String group, String clientName, SocketChannel myClient)
	{
		if (!groupMap.containsKey(group))
		{
			groupMap.put(group, new ArrayList<>());
		}
		
		groupMap.get(group).add(myClient);
		System.out.println(clientName + " is added to group " + group + "!");
	}
	
	private boolean askedForRemovel(String clientMessege)
	{
		return clientMessege.trim().equals("bye!");
	}
	
	private void removeClient(String group, SocketChannel socketToRemove) 
	{
		//remove a socket from a specific group. 
		List<SocketChannel> groupList = groupMap.get(group);
		groupList.remove(socketToRemove);
		//in case the group is empty, remove the group list
		if (groupList.isEmpty())
		{
			groupMap.remove(group);
		}
		try {
			//send a termination message in order to terminate the client read thread
			socketToRemove.write(ByteBuffer.wrap("terminate".getBytes()));
			socketToRemove.close();
		} catch (IOException e) {e.printStackTrace();}
	}
	
	private void broadcast(String group, String name, String messege, SocketChannel sentFrom) 
	{
		List<SocketChannel> groupList = groupMap.get(group);
		for (SocketChannel socketChannel : groupList)
		{	
			try {
				if (!socketChannel.equals(sentFrom))
				{
					//Concatenate the name and the message in order to send to clients
					socketChannel.write(ByteBuffer.wrap((name + ": " + messege).getBytes()));					
				}
			} catch (IOException e) {e.printStackTrace();}
		}
	}
}
