//package secureIM;
/*
 * Implement server functionality as per assignment spec
 */

import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.*;
import javax.xml.bind.DatatypeConverter;
import java.security.*;
import java.security.spec.*;
import javax.crypto.*;
import javax.crypto.spec.*;


public class Server {
	static OutputStream clientOutputStream;
	static InputStream clientInputStream;
	private static Key serverPrivateKey;
	private static Key clientPublicKey;

	static void respondSuccess(String action) {
		PrintStream outputToClient = new PrintStream(clientOutputStream, true);
		outputToClient.println("Success:" + action);
	}

	static void respondFailure(String action) {
		PrintStream outputToClient = new PrintStream(clientOutputStream, true);
		outputToClient.println("Failure:" + action);
	}

	static boolean validateClientInput(String newOrExisting, String usernameField, String passwordField) {
		if (!newOrExisting.equals("New") && !newOrExisting.equals("Existing")) {
			return false;
		}

		if (!usernameField.startsWith("Username:")) {
			return false;
		}

		if (!passwordField.startsWith("Password:")) {
			return false;
		}

		return true;
	}

	static boolean addNewUser(String username, String passwordHashHexString) {
		try {
			if (SecurityHelper.userExists(username)) {
				respondFailure("signup");
				return false;
			}

			FileWriter passwordWriter = new FileWriter("shared_data/user_hashed_passwords.csv", true);
			
			passwordWriter.write(username + "," + passwordHashHexString + "\n");
			passwordWriter.close();

			respondSuccess("signup");
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			respondFailure("signup");
			System.exit(0);
			return false;
		}
	}

	static boolean verifyExistingUser(String username, String passwordHashHexString) {
		try {
			if (!SecurityHelper.userExists(username)) {
				respondFailure("login");
				return false;
			}

			System.out.println("Verifying user identity...");
			String passwordFile = "shared_data/user_hashed_passwords.csv";
			BufferedReader sessionKeyReader = new BufferedReader(new FileReader(passwordFile));

			String line = sessionKeyReader.readLine();
			while (line != null) {
				String[] entries = line.split(",");

				if (entries[0].equals(username)) {

					if (entries[1].equals(passwordHashHexString)) {
						System.out.println("User " + username + " logged in succesfully");
						respondSuccess("login");
						return true;
					} else {
						System.out.println("Incorrect password from Client!");
						respondFailure("login");
						return false;
					}
				}

				line = sessionKeyReader.readLine();
			}

			System.out.println("User does not exist!");
			respondFailure("login");
			return false;

		} catch (IOException e) {
			e.printStackTrace();
			respondFailure("login");
			System.exit(0);
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			respondFailure("login");
			System.exit(0);
			return false;
		}
	}

	// Returns hashed password
	static String handleUserLogin() {
		String passwordHashHexString = new String();
		Scanner clientInputScanner = new Scanner(clientInputStream);

		boolean successful = false;
		do {
			// Read login information from client
			String newOrExisting = new String();
			String usernameField = new String();
			String passwordField = new String();

			try{
				newOrExisting = clientInputScanner.nextLine();
				usernameField = clientInputScanner.nextLine();
				passwordField = clientInputScanner.nextLine();
			} catch(NoSuchElementException e) {
				clientInputScanner.close();
				return "";
			}			

			if (!validateClientInput(newOrExisting, usernameField, passwordField)) {
				System.out.println("Client did not follow correct protocol, exiting");
				System.exit(0);
			}

			String usernameFromClient = usernameField.substring("Username:".length());
			String encryptedPasswordHashFromClient = passwordField.substring("Password:".length());
			byte[] encryptedPasswordHash = SecurityHelper.hexStringToByteArray(encryptedPasswordHashFromClient);
			byte[] passwordHash = SecurityHelper.decryptAssymetric(encryptedPasswordHash, serverPrivateKey);
			passwordHashHexString = SecurityHelper.bytesToHex(passwordHash);

			if (newOrExisting.equals("New")) {
				if (!addNewUser(usernameFromClient, passwordHashHexString)) {
					continue;
				}
			} else if (newOrExisting.equals("Existing")) {
				if (!verifyExistingUser(usernameFromClient, passwordHashHexString)) {
					continue;
				}
			} else {
				System.out.println("Received unexpected message from Client during new or existing check: " + newOrExisting);
				respondFailure("login");
				clientInputScanner.close();
				System.exit(0);
				return "";
			}

			// Need to wait for Client to be done, to ensure that their public key has been added to "trusted public keys"
			String clientReady = clientInputScanner.nextLine();
			if (clientReady.equals("ClientSideLoginDone")) {
				successful = true;
			} else {
				System.out.println("Received unexpected message from Client when waiting for login confirmation: " + clientReady);
				System.exit(0);
			}

			clientPublicKey = SecurityHelper.getUserPublicKey(usernameFromClient);

		} while(!successful);

		return passwordHashHexString;
	}

	// Returns session key/initialization vector pair
	static SecurityHelper.SessionKeyIVPair handleSessionKeyExchange() {
		Scanner clientMessageScanner = new Scanner(clientInputStream);
		String clientMessage = clientMessageScanner.nextLine();

		String messageHeader = "SessionKey:";
		int headerLength = messageHeader.length();
		if (clientMessage.startsWith(messageHeader)) {
			String messageBody = clientMessage.substring(messageHeader.length());
			int commaIndex = messageBody.indexOf(',');

			String encryptedSessionKeyHexString = messageBody.substring(0, commaIndex);
			byte[] encryptedSessionKeyBytes = SecurityHelper.hexStringToByteArray(encryptedSessionKeyHexString);

			byte[] sessionKeyBytes = SecurityHelper.decryptAssymetric(encryptedSessionKeyBytes, serverPrivateKey);
			
			String initializationVectorHexString = messageBody.substring(commaIndex + 1);

			SecretKey sessionKey = new SecretKeySpec(sessionKeyBytes, 0, sessionKeyBytes.length, "AES");

			byte[] initializationVector = DatatypeConverter.parseHexBinary(initializationVectorHexString);

			respondSuccess("sessionkey");

			return new SecurityHelper.SessionKeyIVPair(sessionKey, initializationVector);
		} else {
			System.out.println("Client sent unexpected message during session key exchange: " + clientMessage);
			respondFailure("sessionkey");
			//TODO: don't just exit? keep server up to try again?
			System.exit(0);
			return null;
		}
	}

	static boolean verifyClientModes(HashMap<String, Boolean> modes) {
		Scanner clientMessageScanner = new Scanner(clientInputStream);
		String clientMessage = clientMessageScanner.nextLine();

		String responseIdentifier = "modeverification";
		String messageHeader = "Modes:";

		if (!clientMessage.startsWith(messageHeader)) {
			System.out.println("Client sent unexpected message during mode verification: " + clientMessage);
			respondFailure(responseIdentifier);
			//TODO: don't just exit? keep server up to try again?
			System.exit(0);
			return false;
		}

		String messageBody = clientMessage.substring(messageHeader.length());

		if (modes.get("confidentiality") != messageBody.contains("c")) {
			respondFailure(responseIdentifier);
			return false;
		}

		if (modes.get("integrity") != messageBody.contains("i")) {
			respondFailure(responseIdentifier);
			return false;
		}

		if (modes.get("authentication") != messageBody.contains("a")) {
			respondFailure(responseIdentifier);
			return false;
		}

		respondSuccess(responseIdentifier);
		return true;
	}


	public static void main(String[] args) {
		HashMap<String, Boolean> modes = GeneralHelper.parseCommandLine(args);

		try {

			serverPrivateKey = SecurityHelper.storeKeyPair("server");

			// create server socket
			ServerSocket server = new ServerSocket(8080);
			server.setReuseAddress(true);

			ReadSocketThread receiveMessageThread = null;
			WriteSocketThread sendMessageThread = null;
			MessagingWindow messagingWindow = null;

			while (true) {
				try {
					// wait for client connection
					System.out.println("Awaiting client connection...");
					Socket clientConnection = server.accept();
					System.out.println("Client connected!");

					// create streams for the socket
					clientOutputStream = clientConnection.getOutputStream();
					clientInputStream = clientConnection.getInputStream();

					if(messagingWindow != null) {
						messagingWindow.close();
					}

					if(sendMessageThread != null || receiveMessageThread != null) {
						sendMessageThread.stop();
					}

					if (!verifyClientModes(modes)) {
						System.out.println("Client attempted to start connection with incorrect modes");
						continue;
					}		
					String hashedPasswordFromClient = "";
					
					if(modes.get("confidentiality") || modes.get("integrity") || modes.get("authentication") || modes.get("newUser"))
					{
						hashedPasswordFromClient = handleUserLogin();
						if(hashedPasswordFromClient.equals("")) {
							continue;
						}
					}

					SecurityHelper.SessionKeyIVPair sessionKeyIVPair = new SecurityHelper.SessionKeyIVPair(null, null);

					if (modes.get("confidentiality")) {
						sessionKeyIVPair = handleSessionKeyExchange();
					}

					messagingWindow = GeneralHelper.createUI("Server");

					receiveMessageThread = new ReadSocketThread("receive-messages", 
							clientInputStream, modes, clientPublicKey, sessionKeyIVPair, messagingWindow);

					receiveMessageThread.start();

					sendMessageThread = new WriteSocketThread("send-messages",
						clientOutputStream, modes, serverPrivateKey, sessionKeyIVPair, messagingWindow);

					sendMessageThread.start();

					if (modes.get("integrity") || modes.get("authentication")) {
						// Get client's public key
					}
				} catch (java.net.SocketException e) {
					System.out.println(e);
				}
			} //end while
		} catch (java.net.SocketException e) {
			System.out.println("SocketException: " + e);
			System.exit(0);
		} catch (IOException e) {
			System.out.println("IOException: " + e);
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
}