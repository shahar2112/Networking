package il.co.ilrd.Networking;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class WebPage {
	
	public static void main(String args[]) throws IOException
	{
		download("http://www.google.com");
	}
	
	public static void download(String urlString) throws IOException
	{
		URL url = new URL(urlString);
		try(
		BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
		BufferedWriter writer = new BufferedWriter(new FileWriter("page.html"));
		)
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				writer.write(line);
			}
		}
		 System.out.println("Page downloaded.");
	}
}
