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
	private static PrivateKey privKey;
	private static PublicKey pubKey;
	private static PublicKey serverPubKey;

	// Returns the hashed password of the user
	static String handleLogin(boolean newUser) {
		Scanner sc = new Scanner(System.in);
		PrintStream outputToServer = new PrintStream(serverOutputStream);

		String qualifier = newUser ? "a" : "your";

		String passwordHashString = new String();

		boolean successful = false;
		do {

			// get user's username
			System.out.print("Please enter " + qualifier + " username: ");
			String username = sc.nextLine();
			
			String plaintextPassword;

			Console console = System.console();

			// get user's password
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

			// Send user login information to server
			if (newUser) {
				outputToServer.println("New");
			} else {
				outputToServer.println("Existing");
			}
			outputToServer.println("Username:" + username);

			String encryptedPasswordHashString = SecurityHelper.encryptWithPublicKey(passwordHashString, serverPubKey);

			// outputToServer.println("Password:" + encryptedPasswordHashString);
			outputToServer.println("Password:" + passwordHashString);
			outputToServer.flush();

			// Wait for login response from server
			Scanner clientResponseScanner = new Scanner(serverInputStream);
			String response = clientResponseScanner.nextLine();
			
			// Parse server response to login
			if (newUser) {
				if (response.startsWith("Success:signup")) {
					System.out.println("Account created successfully!");
					successful = true;
				} else if (response.startsWith("Failure:signup")) {
					System.out.println("Account creation failed, please try again");
					continue;
				} else if (response.startsWith("Failure:exists")) {
					System.out.println("Username already exists, please try again");
					continue;
				}
				else {
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

	// returns session key/initialization vector pair
	static GeneralHelper.SessionKeyIVPair handleSessionKeyExchange(String passwordHash) {

		SecretKey key = SecurityHelper.generatePasswordBasedKey(passwordHash);
		String keyHexString = DatatypeConverter.printHexBinary(key.getEncoded());

		SecureRandom random = new SecureRandom();
		byte[] initializationVector = new byte[16];
		random.nextBytes(initializationVector);
		String initializationVectorHexString = DatatypeConverter.printHexBinary(initializationVector);

		String message = "SessionKey:" + keyHexString + "," + initializationVectorHexString;

		// TODO important: encrypt this message with server's public key!

		PrintStream outputToServer = new PrintStream(serverOutputStream);
		outputToServer.println(message);

		Scanner serverResponseScanner = new Scanner(serverInputStream);
		String serverResponse = serverResponseScanner.nextLine();

		System.out.println("Session key: " + keyHexString);
		System.out.println("IV: " + initializationVectorHexString);

		if (serverResponse.startsWith("Success:sessionkey")) {
			System.out.println("Session key exchange succeeded");
			return new GeneralHelper.SessionKeyIVPair(key, initializationVector);
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

			serverPubKey = SecurityHelper.getUserPublicKey("server");
			KeyPair clientKP = SecurityHelper.generateUserKeyPair();

			Socket serverConnection = new Socket("localhost", 8080);
			serverOutputStream = serverConnection.getOutputStream();
			serverInputStream = serverConnection.getInputStream();

			String passwordHash = handleLogin(modes.get("newUser"));

			GeneralHelper.SessionKeyIVPair sessionKeyIVPair = new GeneralHelper.SessionKeyIVPair(null, null);

			if (modes.get("confidentiality")) {
				sessionKeyIVPair = handleSessionKeyExchange(passwordHash);
			}

			ReadSocketThread receiveMessageThread = new ReadSocketThread("receive-messages", 
					serverInputStream, modes, null, sessionKeyIVPair);
			receiveMessageThread.start();

			WriteSocketThread sendMessageThread = new WriteSocketThread("send-messages", 
					serverOutputStream, modes, null, sessionKeyIVPair);
			sendMessageThread.start();

			while(true);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception: " + e);
		}
	}
}