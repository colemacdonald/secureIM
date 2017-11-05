//package secureIM;
/*
 * Implement server functionality as per assignment spec
 */

import java.net.*;
import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.lang.Thread;
import java.lang.StringBuffer;


public class Server {

	private static StringBuffer inputBuffer;// = StringBuffer();

	public static void main(String[] args) {
		HashMap<String, Boolean> modes = GeneralHelper.parseCommandLine(args);

		
		try {
			ServerSocket server = new ServerSocket(8080);
			server.setReuseAddress(true);
			System.out.println("Waiting for client...");

			while (true) {
				try {
					Socket clientConnection = server.accept();
					System.out.println("Client connected!");

					OutputStream userInputStream = clientConnection.getOutputStream();
					InputStream clientInputStream = clientConnection.getInputStream();
					BufferedReader bRead = new BufferedReader(new InputStreamReader(clientInputStream));
					
					/*
					String line = bRead.readLine();

					String flag_strings[] = line.split(" ");

					if (modes.get("confidentiality") != Boolean.parseBoolean(flag_strings[0]) 
							|| modes.get("integrity") != Boolean.parseBoolean(flag_strings[1]) 
							|| modes.get("availability") != Boolean.parseBoolean(flag_strings[2])) {

						System.out.println("Client modes do not match Server modes; closing connection.");
						continue;
					}
					*/
					ReadSocketThread receiveMessageThread = new ReadSocketThread("receive-messages", clientInputStream);
					receiveMessageThread.start();

					WriteSocketThread sendMessageThread = new WriteSocketThread("send-messages", userInputStream);
					sendMessageThread.start();
					/*
					try {
						receiveMessageThread.join();
						sendMessageThread.join();
						System.out.println("Both threads joined??");
					} catch (InterruptedException e) {
						System.out.println(e);
						continue;
					} */
				} catch (java.net.SocketException e) {
					System.out.println(e);
				}
			}
		} catch (java.net.SocketException e) {
			System.out.println("SocketException: " + e);
			System.exit(0);
		} catch (IOException e) {
			System.out.println("IOException: " + e);
			System.exit(0);
		}		
	}
}