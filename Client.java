/*
Implement client functionality as per assignment spec

*/
import java.net.*;
import java.security.*;
import javax.crypto.*;
import java.io.*;

public class Client {
	public static void main(String[] args) {
		
		boolean c = false;
		boolean i = false;
		boolean a = false;

		for (int j = 0; j < args.length; j++){
			if (args[j].contains("c"))
				c = true;
			if (args[j].contains("i"))
				i = true;
			if (args[j].contains("a"))
				a = true;
		}

		System.out.println(String.valueOf(c) + " + " + String.valueOf(i) + " + " + String.valueOf(a));

		try {
			Socket serverConnection = new Socket("localhost", 8080);
			OutputStream outStream = serverConnection.getOutputStream();
			PrintStream printStream = new PrintStream(outStream, true);
			printStream.println(String.valueOf(c) + " + " + String.valueOf(i) + " + " + String.valueOf(a));

		} catch (Exception e) {
			System.out.println("Exception: " + e);
		}
	}
}