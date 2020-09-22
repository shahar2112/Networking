package il.co.ilrd.Networking.ChatServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ClientIlai {

	private String name;
	private String group;
	private volatile boolean isRegistered;
	private volatile boolean shouldRun;
	private String prefixMessage;
	private SocketChannel socket;
	
	public ClientIlai(String name, String group) {
		this.shouldRun = true;
		this.isRegistered = false;
		this.name = name;
		this.group = group;
		this.prefixMessage = getPrefix(this.name, this.group);
		
		InetSocketAddress address = new InetSocketAddress("localhost", 11111);
		
		try {
			this.socket = SocketChannel.open(address);
		} catch (IOException e) {
			System.out.println("Couldn't connect to server");
			e.printStackTrace();
		}
		
		new Thread(new SendData()).start();
		new Thread(new CollectData()).start();
	}
	
	private String getPrefix(String group, String name) {
		return "%" + this.group + "%" +this.name + ": %";
	}
	
	class SendData implements Runnable {
		
		@Override
		public void run() {
			ByteBuffer outBuffer = null;
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			while (shouldRun) {
				try {
					if(!isRegistered) {
						handleRegister();
						continue;
					}
					
					String prefix = "1" + prefixMessage;
					String message = in.readLine();
					
					if (message.equalsIgnoreCase("bye")) {
						in.close();
						shouldRun = false;
					}
					
					if(socket.isOpen()) {
						message = prefix + message;
						outBuffer = ByteBuffer.wrap(message.getBytes());
						socket.write(outBuffer);
						outBuffer.clear();
					}
					
				} catch (IOException e) {
					System.out.println("Couldn't write message to server");
					e.printStackTrace();
				}
			}
		}
		
		private void handleRegister() {
			ByteBuffer outBuffer = null;
			String registerMessage = "0" + prefixMessage;
			outBuffer = ByteBuffer.wrap(registerMessage.getBytes());
			try {
				socket.write(outBuffer);
			} catch (IOException e) {
				System.out.println("Couldn't register client");
				e.printStackTrace();
			}
			isRegistered = true;
		}
	}
	
	class CollectData implements Runnable {

		@Override
		public void run() {
			
			while (shouldRun) {
				try {
					ByteBuffer inBuffre = ByteBuffer.allocate(256);
					inBuffre.clear();
					socket.read(inBuffre);
					String message = new String(inBuffre.array()).trim();
					if(message.equalsIgnoreCase("bye")) {
						socket.close();
						shouldRun = false;
						break;
					}
					System.out.println(message);
					
				} catch (IOException e) {
					System.out.println("Couldn't read message from server");
					e.printStackTrace();
				}
			}
		}	
	}
}

