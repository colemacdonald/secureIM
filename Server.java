/*
 * Implement server functionality as per assignment spec
 */

import java.net.*;
import java.security.*;
import javax.crypto.*;
import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.lang.Thread;
import java.lang.StringBuffer;


public class Server {

	private static StringBuffer inputBuffer;// = StringBuffer();
	private static InputThread t;// = InputThread("user-in", inputBuffer);
	private static Thread thread;

	public static void main(String[] args) {
		HashMap<String, Boolean> modes = Helper.parseCommandLine(args);

		/*			TESTING InputThread 		*/
		inputBuffer = new StringBuffer();
		t = new InputThread("user-in", inputBuffer);

		t.start();

		for(int i = 0; i < 9999999; i++)
		{
			if(!inputBuffer.toString().equals(""))
			{
				System.out.println("\n Main reading input as: " + inputBuffer.toString());
				inputBuffer.setLength(0);
			}
		}
		/*			END TEST 			*/

		
		try {
			ServerSocket server = new ServerSocket(8080);
			server.setReuseAddress(true);
			System.out.println("Waiting for client...");

			while (true) {
				try {
					Socket clientConnection = server.accept();
					System.out.println("Client connected!");					

					InputStream inStream = clientConnection.getInputStream();
					BufferedReader bRead = new BufferedReader(new InputStreamReader(inStream));
					String line = bRead.readLine();

					String flag_strings[] = line.split(" ");

					if (modes.get("confidentiality") != Boolean.parseBoolean(flag_strings[0]) 
							|| modes.get("integrity") != Boolean.parseBoolean(flag_strings[1]) 
							|| modes.get("availability") != Boolean.parseBoolean(flag_strings[2])) {

						System.out.println("Client modes do not match Server modes; closing connection.");
						server.close();
					}

					
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