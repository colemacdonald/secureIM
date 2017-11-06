//package secureIM;

/*
 * Implement client functionality as per assignment spec
 */

import java.net.*;
import java.io.*;
import java.util.*;
import javax.xml.bind.DatatypeConverter;
import java.security.*;
import java.security.spec.*;
import javax.crypto.*;
import javax.crypto.spec.*;

public class Client {

	// Returns the hashed password of the user
	static byte[] handleLogin(boolean newUser, OutputStream clientOutputStream, InputStream clientInputStream) {
		Scanner sc = new Scanner(System.in);
		PrintStream outputToServer = new PrintStream(clientOutputStream);

		String qualifier = newUser ? "a" : "your";

		boolean successful = false;
		byte[] passwordHash = {};

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

			if (plaintextPassword.length() < 8) {
				System.out.println("Password must be at least 8 characters, please try again");
				continue;
			}

			passwordHash = SecurityHelper.computeDigest(plaintextPassword.getBytes());
			String passwordHashString = DatatypeConverter.printHexBinary(passwordHash);

			if (newUser) {
				outputToServer.println("New");
			} else {
				outputToServer.println("Existing");
			}
			outputToServer.println("Username:" + username);
			// TODO: Encrypted with server's public key
			outputToServer.println("Password:" + passwordHashString);
			outputToServer.flush();

			Scanner clientResponseScanner = new Scanner(clientInputStream);
			String response = clientResponseScanner.nextLine();
			
			if (response.equals("Success")) {
				if (newUser) {
					System.out.println("Account created successfully!");
				} else {
					System.out.println("Logged in successfully");
				}
				successful = true;
			} else if (response.equals("Failure")) {
				// TODO: more informative error messages
				if (newUser) {
					System.out.println("Account creation failed, please try again");
				} else {
					System.out.println("Log in failed, please try again");
				}
				continue;
			} else {
				System.out.println("Received unexpected message from Server: " + response);
				System.out.println("Exiting");
				System.exit(0);
			}
		}

		if (passwordHash.length == 0) {
			System.out.println("Something went wrong with login handling, exiting");
			System.exit(0);
		}
		
		return passwordHash;
	}

	public static void main(String[] args) {
		HashMap<String, Boolean> modes = GeneralHelper.parseCommandLine(args);

		try {
			Socket serverConnection = new Socket("localhost", 8080);
			OutputStream outStream = serverConnection.getOutputStream();
			InputStream inStream = serverConnection.getInputStream();

			byte[] passwordHash = handleLogin(modes.get("newUser"), outStream, inStream);

			PrintStream printStream = new PrintStream(outStream, true);
			printStream.println("testing send");

			/* TESTING MSG SEND */

			String password = "password";
			SecureRandom random = new SecureRandom();
			byte[] initializationVector = {-18, 8, -18, -62, -95, -64, 36, -17, -67, 67, 87, 25, -18, -15, -38, 81};//new byte[16];
			//random.nextBytes(initializationVector);
	
			// SecretKey sessionKey = SecurityHelper.generatePasswordBasedKey(password);
			// String encodedKey = Base64.getEncoder().encodeToString(sessionKey.getEncoded());
			// System.out.println(encodedKey);
			//byte[] decodedKey = Base64.getDecoder().decode("B0FZlSHiUEKsInRxJCJwm7yXXy7MpcVpX6yCxBGjrCw=");
			// rebuild key using SecretKeySpec
			SecretKey sessionKey = new SecretKeySpec(passwordHash, 0, passwordHash.length, "AES");

			ReadSocketThread receiveMessageThread = new ReadSocketThread("receive-messages", 
				inStream, modes, sessionKey, sessionKey, initializationVector);
			receiveMessageThread.start();

			WriteSocketThread sendMessageThread = new WriteSocketThread("send-messages", outStream,
				modes, sessionKey, sessionKey, initializationVector);
			sendMessageThread.start();

			while(true);
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		}
	}
}