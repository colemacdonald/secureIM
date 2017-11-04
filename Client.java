package secureIM;

/*
 * Implement client functionality as per assignment spec
 */

import java.net.*;
import java.io.*;
import java.util.HashMap;
import static secureIM.GeneralHelper.*;

public class Client {
	public static void main(String[] args) {
		HashMap<String, Boolean> modes = parseCommandLine(args);		

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