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
	static OutputStream serverOutputStream;
	static InputStream serverInputStream;

	// Returns the hashed password of the user
	static String handleLogin(boolean newUser) {
		Scanner sc = new Scanner(System.in);
		PrintStream outputToServer = new PrintStream(serverOutputStream);

		String qualifier = newUser ? "a" : "your";

		String passwordHashString = new String();

		boolean successful = false;
		do {
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

			byte[] passwordHash = SecurityHelper.computeDigest(plaintextPassword.getBytes());
			passwordHashString = DatatypeConverter.printHexBinary(passwordHash);

			if (newUser) {
				outputToServer.println("New");
			} else {
				outputToServer.println("Existing");
			}
			outputToServer.println("Username:" + username);
			// TODO: Encrypted with server's public key
			outputToServer.println("Password:" + passwordHashString);
			outputToServer.flush();

			Scanner clientResponseScanner = new Scanner(serverInputStream);
			String response = clientResponseScanner.nextLine();
			
			if (newUser) {
				if (response.startsWith("Success:signup")) {
					System.out.println("Account created successfully!");
					successful = true;
				} else if (response.startsWith("Failure:signup")) {
					System.out.println("Account creation failed, please try again");
					continue;
				} else {
					System.out.println("Received unexpected message from Server: " + response);
					System.out.println("Exiting");
					System.exit(0);
				}
			} else {
				if (response.startsWith("Success:login")) {
					System.out.println("Logged in successfully");
					successful = true;
				} else if (response.startsWith("Failure:login")) {
					System.out.println("Log in failed, please try again");
				} else {
					System.out.println("Received unexpected message from Server: " + response);
					System.out.println("Exiting");
					System.exit(0);
				}
			}
		} while (!successful);

		if (passwordHashString.length() == 0) {
			System.out.println("Something went wrong with login handling, exiting");
			System.exit(0);
		}

		return passwordHashString;
	}

	static SecretKey handleSessionKeyExchange(String passwordHash) {
		SecretKey key = SecurityHelper.generatePasswordBasedKey(passwordHash);
		String keyHexString = DatatypeConverter.printHexBinary(key.getEncoded());

		String message = "SessionKey:" + keyHexString;

		// TODO important: encrypt this message with server's public key!

		System.out.println("Sending session key message: " + message);

		PrintStream outputToServer = new PrintStream(serverOutputStream);
		outputToServer.println(message);

		Scanner serverResponseScanner = new Scanner(serverInputStream);
		String serverResponse = serverResponseScanner.nextLine();

		System.out.println("Received session key message: " + serverResponse);

		if (serverResponse.startsWith("Success:sessionkey")) {
			System.out.println("Session key exchange succeeded");
			return key;
		} else if (serverResponse.startsWith("Failure:sessionkey")) {
			System.out.println("Session key exchange failed, exiting");
			System.exit(0);
			return null;
		} else {
			System.out.println("Received unexpected message from Server: " + serverResponse);
			System.out.println("Exiting");
			System.exit(0);
			return null;
		}
	}

	public static void main(String[] args) {
		HashMap<String, Boolean> modes = GeneralHelper.parseCommandLine(args);

		try {
			Socket serverConnection = new Socket("localhost", 8080);
			serverOutputStream = serverConnection.getOutputStream();
			serverInputStream = serverConnection.getInputStream();

			String passwordHash = handleLogin(modes.get("newUser"));

			if (modes.get("confidentiality")) {
				SecretKey sessionKey = handleSessionKeyExchange(passwordHash);
			}

			/* TESTING MSG SEND */

			// String password = "password";
			// SecureRandom random = new SecureRandom();
			// byte[] initializationVector = {-18, 8, -18, -62, -95, -64, 36, -17, -67, 67, 87, 25, -18, -15, -38, 81};//new byte[16];
			// //random.nextBytes(initializationVector);
	
			// // SecretKey sessionKey = SecurityHelper.generatePasswordBasedKey(password);
			// // String encodedKey = Base64.getEncoder().encodeToString(sessionKey.getEncoded());
			// // System.out.println(encodedKey);
			// //byte[] decodedKey = Base64.getDecoder().decode("B0FZlSHiUEKsInRxJCJwm7yXXy7MpcVpX6yCxBGjrCw=");
			// // rebuild key using SecretKeySpec
			// //SecretKey sessionKey = new SecretKeySpec(passwordHash, 0, passwordHash.length, "AES");

			// ReadSocketThread receiveMessageThread = new ReadSocketThread("receive-messages", 
			// 	inStream, modes, sessionKey, sessionKey, initializationVector);
			// receiveMessageThread.start();

			// WriteSocketThread sendMessageThread = new WriteSocketThread("send-messages", outStream,
			// 	modes, sessionKey, sessionKey, initializationVector);
			// sendMessageThread.start();

			while(true);
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		}
	}
}