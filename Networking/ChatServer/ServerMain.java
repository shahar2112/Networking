package il.co.ilrd.Networking.ChatServer;

public class ServerMain 
{
	public static void main(String[] args) 
	{
		ChatServerImp myServer = new ChatServerImp(3);
		
		myServer.startServer();
	}
}
