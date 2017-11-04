/*
 * Implement client functionality as per assignment spec
 */

import java.net.*;
import java.security.*;
import javax.crypto.*;
import java.io.*;
import java.util.HashMap;

public class Client {
	public static void main(String[] args) {
		HashMap<String, Boolean> modes = Helper.parseCommandLine(args);		

		try {
			Socket serverConnection = new Socket("localhost", 8080);
			OutputStream outStream = serverConnection.getOutputStream();
			PrintStream printStream = new PrintStream(outStream, true);
			//printStream.println(String.valueOf(c) + " " + String.valueOf(i) + " " + String.valueOf(a));

		} catch (Exception e) {
			System.out.println("Exception: " + e);
		}
	}

}