/*
 * Implement server functionality as per assignment spec
 */

import java.net.*;
import java.security.*;
import javax.crypto.*;
import java.io.*;
import java.util.Arrays;
import java.lang.Thread;
import java.lang.StringBuffer;

//dialog boxes
import javax.swing.JOptionPane;

public class Server {

	private static StringBuffer inputBuffer;// = StringBuffer();
	private static InputThread t;// = InputThread("user-in", inputBuffer);
	private static Thread thread;
	private static boolean c = false;
	private static boolean i = false;
	private static boolean a = false;

	static void printUsage()
	{
		System.out.println("Invalid use. Usage:\njava Server [-cia]");
		System.out.println("c enables encryption, i enables data integrity, a enables authentication");
		System.out.println("undocumented arguments will be ignored");
	}

	/* parse command line for c i a, store results in boolean values c i a */
	static void parseCommandLine(String[] args)
	{
		if(args.length < 0) {
			printUsage();
			System.exit(0);
		}

		a = args[0].contains("a");
		c = args[0].contains("c");
		i = args[0].contains("i");
	}

	public static void main(String[] args) {
		parseCommandLine(args);

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
			System.out.println("Waiting for client...");
			Socket clientConnection = server.accept();
			System.out.println("Client connected!");					

			InputStream inStream = clientConnection.getInputStream();
			BufferedReader bRead = new BufferedReader(new InputStreamReader(inStream));
			String line = bRead.readLine();

			String flag_strings[] = line.split(" ");

			if (c != Boolean.parseBoolean(flag_strings[0]) || i != Boolean.parseBoolean(flag_strings[1]) || a != Boolean.parseBoolean(flag_strings[2])){
				System.out.println("Client cia does not match Server cia");
				System.exit(0);
			}



		} catch (Exception e) {
			System.out.println("Exception: " + e);
		}
	}
}