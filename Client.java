//package secureIM;

/*
 * Implement client functionality as per assignment spec
 */

import java.net.*;
import java.io.*;
import java.util.*;
import javax.xml.bind.DatatypeConverter;

public class Client {

	static void handleLogin(boolean newUser, OutputStream outStream) {
		Scanner sc = new Scanner(System.in);
		PrintStream outputToServer = new PrintStream(outStream);

		String qualifier = newUser ? "a" : "your";

		boolean successful = false;

		while (!successful) {
			System.out.print("Please enter " + qualifier + " username: ");
			String username = sc.nextLine();

			String plaintextPassword;

			Console console = System.console();

			if (console == null) {
				System.out.println("WARNING: Your console application does not support concealing password fields!!!");
				System.out.print("Please enter " + qualifier + " password: ");
				plaintextPassword = sc.nextLine();
			} else {
				char[] password1 = console.readPassword("Please enter " + qualifier + " password: ");

				if (newUser) {
					char[] password2 = console.readPassword("Please repeat your password: ");
					if (!Arrays.equals(password1, password2)) {
						System.out.println("Passwords did not match, please try again");
						continue;
					}
				}
				plaintextPassword = String.valueOf(password1);
			}

			byte[] passwordHash = SecurityHelper.computeDigest(plaintextPassword.getBytes());
			String passwordHashString = DatatypeConverter.printHexBinary(passwordHash);

			// TODO: Send this to server (encrypted with server's public key), rather than writing to shared file
			if (newUser) {
				outputToServer.println("New");
			} else {
				outputToServer.println("Existing");
			}
			outputToServer.println("Username:" + username);
			outputToServer.println("Password:" + passwordHashString);
			outputToServer.flush();

			successful = true;
		}
	}

	public static void main(String[] args) {
		HashMap<String, Boolean> modes = GeneralHelper.parseCommandLine(args);		

		try {
			Socket serverConnection = new Socket("localhost", 8080);
			OutputStream outStream = serverConnection.getOutputStream();
			InputStream inStream = serverConnection.getInputStream();

			handleLogin(modes.get("newUser"), outStream);

			PrintStream printStream = new PrintStream(outStream, true);
			printStream.println("testing send");

			ReadSocketThread receiveMessageThread = new ReadSocketThread("client-read", inStream, modes);

			WriteSocketThread sendMessageThread = new WriteSocketThread("client-write", outStream, modes);

			while(true);
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		}
	}
}