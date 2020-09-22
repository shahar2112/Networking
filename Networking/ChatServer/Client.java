package il.co.ilrd.Networking.ChatServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class Client 
{
	private static final int BUFFER_SIZE = 1024;
	private String name;
	private String group;
	private SocketChannel mySocketChannel;
	boolean toStop;
	
	public Client(String name, String group) 
	{
		this.name = name;
		this.group = group;
		toStop = false;
		
		int port = 9999;
		InetAddress hostIP;
		try {
			hostIP = InetAddress.getLocalHost();
			// Creating a "server socket address" (object of server IP + server port).
			InetSocketAddress ServerAdress = new InetSocketAddress(hostIP, port);
			// Creating a socketChannel and connect it to the serverAdress
			mySocketChannel = SocketChannel.open(ServerAdress);
			//sending a registration message in order to add the client to a proper group in serverchat
			sendRegisterMessege();
			
			new Thread(new sendMessege()).start();
			new Thread(new collectMessege()).start();
			
		} catch (IOException e) {e.printStackTrace();}
	}
	
	private class sendMessege implements Runnable
	{
		@Override
		public void run() 
		{
			Scanner scanner = new Scanner(System.in);
			while (!toStop)
			{
				String Input = scanner.nextLine();
				
				if (Input.equalsIgnoreCase("bye!"))
				{
					toStop = true;
				}
				
				//Concatenate the group name and user name with the input message
				String toSend = (group + "@" + name + "@ ");
				toSend = toSend.concat(Input);
				
				try {
					mySocketChannel.write(ByteBuffer.wrap(toSend.getBytes()));
				} catch (IOException e) {e.printStackTrace();}	
			}
			
			scanner.close();
		}
	}
	
	private class collectMessege implements Runnable
	{
		@Override
		public void run() 
		{
			while (!toStop)
			{
				ByteBuffer myBuffer = ByteBuffer.allocate(BUFFER_SIZE);
				
				// reads from channel to myBuffer
				try {
					mySocketChannel.read(myBuffer);
				} catch (IOException e) {e.printStackTrace();}
				
				String messege = new String(myBuffer.array());
				if (!isTerminationMessege(messege))
				{
					System.out.println(messege);						
				}
			}
			
			try {
				mySocketChannel.close();
			} catch (IOException e) {e.printStackTrace();}
		}
	}
	
	private void sendRegisterMessege() 
	{
		String toSend = (group + "@" + name + "@ " + "##register##");		
		try {
			mySocketChannel.write(ByteBuffer.wrap(toSend.getBytes()));
		} catch (IOException e) {e.printStackTrace();}	
	}
	
	private boolean isTerminationMessege(String messege)
	{
		return messege.trim().equalsIgnoreCase("terminate");
	}
}
