package il.co.ilrd.Networking.ChatServer;

	import java.io.IOException;
	import java.net.InetSocketAddress;
	import java.nio.ByteBuffer;
	import java.nio.channels.SelectionKey;
	import java.nio.channels.Selector;
	import java.nio.channels.ServerSocketChannel;
	import java.nio.channels.SocketChannel;
	import java.util.ArrayList;
	import java.util.HashMap;
	import java.util.Iterator;
	import java.util.List;
	import java.util.Map;
	import java.util.Map.Entry;
	import java.util.Set;

	


	//message format: 1%group%name: %message
	// if 1  it's registered

	public class ChatServerIlai implements ChatServer{

		private Map<String, List<SocketChannel>> groupMap;
		private Selector selector;
		private ServerSocketChannel serverSocketChannel;
		private volatile boolean shouldRun;
		
		public ChatServerIlai() {
			this.groupMap = new HashMap<String, List<SocketChannel>>();
			this.shouldRun = true;
			try {
				selector = Selector.open();
				serverSocketChannel = ServerSocketChannel.open();
				
				InetSocketAddress address = new InetSocketAddress("localhost", 11111);
				
				serverSocketChannel.bind(address);
				serverSocketChannel.configureBlocking(false);
				int validOps = serverSocketChannel.validOps();
					
				@SuppressWarnings("unused")
				SelectionKey key = serverSocketChannel.register(selector, validOps, null);
				
			} catch (IOException e) {
				System.out.println("Could not initialize server");
				e.printStackTrace();
			}
		}
		@Override
		public void startServer() {
			while (shouldRun) {
				try {
					selector.select();
					Set<SelectionKey> selectedKeys = selector.selectedKeys();
					Iterator<SelectionKey> iter = selectedKeys.iterator();
						
					while (iter.hasNext()) {
						SelectionKey currKey = iter.next();
						
						if (currKey.isAcceptable()) {
							handleAccept();
						}
						else if (currKey.isReadable()) {
							handleRead(currKey);
						}
						iter.remove();
					}
				} catch (IOException e) {
					System.out.println("Couldn't select key");
					e.printStackTrace();
				}
			}
			shutDownClients();
			try {
				serverSocketChannel.close();
				selector.close();
			} catch (IOException e) {
				System.out.println("Couldn't close ServerSocket on shutdown");
				e.printStackTrace();
			}
		}
		
		@Override
		public void stopServer() {
			this.shouldRun = false;
			
			try {
				@SuppressWarnings("unused")
				SocketChannel badApple = SocketChannel.open(new InetSocketAddress("localhost", 1111));
			} catch (IOException e) {
				System.out.println("Couldn't create bad apple");
				e.printStackTrace();
			}
		}
		
		private void shutDownClients() {
			Set<Entry<String, List<SocketChannel>>> entrySet = groupMap.entrySet();
			
			for (Entry<String, List<SocketChannel>> entry : entrySet) {
				broadCast(entry.getKey(), "Server shutting down", "", null);
				List<SocketChannel> socketList = entry.getValue();
				for (SocketChannel socket : socketList) {
					ByteBuffer bye = ByteBuffer.wrap("bye".getBytes());
					try {
						socket.write(bye);
						socket.close();
					} catch (IOException e) {
						System.out.println("Couldn't close socket on shutdown");
						e.printStackTrace();
					}
				}
			}
		}
		
		private void handleAccept() {
			SocketChannel client;
			try {
				client = serverSocketChannel.accept();
				if (null != client) {
					client.configureBlocking(false);
					client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
				}
			} catch (IOException e) {
				System.out.println("Couldn't handle accept");
				e.printStackTrace();
			}
		}
		
		private void handleRead(SelectionKey currKey) {
			SocketChannel client = (SocketChannel) currKey.channel();
			ByteBuffer inBuffer = ByteBuffer.allocate(256);
			
			try {
				client.read(inBuffer);
				String rawMessage = new String(inBuffer.array()).trim();
				inBuffer.clear();
				String[] messageData = parseRawMessage(rawMessage);
				
				if(!isRegisterMessage(messageData)) {
					registerClient(getGroup(messageData), getName(messageData), client);
					return;
				}
				
				if(getMessage(messageData).equalsIgnoreCase("bye")) {
					handleRemove(messageData, client);
					return;
				}
				
				broadCast(getGroup(messageData),
						getMessage(messageData),
						getName(messageData),
						client);
				
			}catch (IOException e) {
				System.out.println("Couldn't read message from client");
				e.printStackTrace();
			}
		}
		
		private void handleRemove(String[] messageData, SocketChannel client) {
			broadCast(getGroup(messageData),
					"has left the group",
					getName(messageData),
					client);
			
			groupMap.get(getGroup(messageData)).remove(client);
			ByteBuffer bye = ByteBuffer.wrap("bye".getBytes());
			try {
				client.write(bye);
				client.close();
			} catch (IOException e) {
				System.out.println("Couldn't remove client");
				e.printStackTrace();
			}
			
		}
		
		private void broadCast(String group, String message, String name, SocketChannel senderSocket) {
			List<SocketChannel> groupList = groupMap.get(group);
			ByteBuffer outBuffer = ByteBuffer.allocate(256);
			String out = name + message;
			outBuffer = ByteBuffer.wrap(out.getBytes());
			Iterator<SocketChannel> iter = groupList.iterator();
			
			while (iter.hasNext()) {
				SocketChannel dst = iter.next();
				if (dst.equals(senderSocket)) {
					continue;
				}
				try {			
					dst.write(outBuffer);
					outBuffer.clear();
				} catch (IOException e) {
					System.out.println("Couldn't send the message");
					e.printStackTrace();
				}
			}
		}
		
		private String getGroup(String[] messageData) {
			
			return messageData[1];
		}
		
		private String getName(String[] messageData ) {
			return messageData[2];
		}
		
		private String getMessage(String[] messageData) {
			
			String message ="";
			
			for (int i = 3; i < messageData.length; ++i) {
				message += messageData[i];
			}
			return message;
		}
		
		private boolean isRegisterMessage (String[] messageData) {
			return messageData[0].charAt(0) == '1';
		}
		
		private void registerClient(String groupName, String name, SocketChannel senderSocket) {
			if (groupMap.containsKey(groupName)) {
				groupMap.get(groupName).add(senderSocket);
			}
			else {
				ArrayList<SocketChannel> list = new ArrayList<>();
				list.add(senderSocket);
				groupMap.put(groupName, list);
			}
			
			broadCast(groupName, "has joined the group", name, senderSocket);
		}
		
		private String[] parseRawMessage(String rawMessage) {
			return rawMessage.split("%");
		}
		
}
