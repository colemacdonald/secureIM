//package secureIM;

/*
 * Implement client functionality as per assignment spec
 */

import java.net.*;
import java.io.*;
import java.util.HashMap;

public class Client {
	public static void main(String[] args) {
		HashMap<String, Boolean> modes = GeneralHelper.parseCommandLine(args);		

		try {
			Socket serverConnection = new Socket("localhost", 8080);
			OutputStream outStream = serverConnection.getOutputStream();
			PrintStream printStream = new PrintStream(outStream, true);

			while(true);
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		}
	}

}