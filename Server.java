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
	private static PrivateKey privateKey;

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

	static boolean addNewUser(String username, String passwordHash) {
		try {
			System.out.println(username);
			if (SecurityHelper.userExists(username)) {
				respondFailure("exists");
				return false;
			}

			FileWriter passwordWriter = new FileWriter("shared_data/user_hashed_passwords.csv", true);
			passwordWriter.write(username + "," + passwordHash + "\n");
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

	static boolean verifyExistingUser(String username, String passwordHash) {
		try {
			System.out.println("Verifying user identity...");
			String passwordFile = "shared_data/user_hashed_passwords.csv";
			BufferedReader sessionKeyReader = new BufferedReader(new FileReader(passwordFile));

			String line = sessionKeyReader.readLine();
			while (line != null) {
				String[] entries = line.split(",");

				if (entries[0].equals(username)) {
					// decrypt password hash from client
					String decryptedPasswordHash = SecurityHelper.decryptWithPrivateKey(passwordHash, privateKey);

					if (entries[1].equals(decryptedPasswordHash)) {
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
		String hashedPasswordFromClient = "";

		boolean successful = false;
		do {
			// Read login information from client
			Scanner clientInputScanner = new Scanner(clientInputStream);
			String newOrExisting = "";
			String usernameFromClient = "";

			try{
				newOrExisting = clientInputScanner.nextLine();
				usernameFromClient = clientInputScanner.nextLine();
				hashedPasswordFromClient = clientInputScanner.nextLine();
			} catch(NoSuchElementException e) {
				System.out.println("caught");
				clientInputScanner.close();
				return "";
			}			

			if (!validateClientInput(newOrExisting, usernameFromClient, hashedPasswordFromClient)) {
				System.out.println("Client did not follow correct protocol, exiting");
				System.exit(0);
			}

			usernameFromClient = usernameFromClient.substring("Username:".length());
			hashedPasswordFromClient = hashedPasswordFromClient.substring("Password:".length());

			if (newOrExisting.equals("New")) {
				if (!addNewUser(usernameFromClient, hashedPasswordFromClient)) {
					continue;
				}
			} else if (newOrExisting.equals("Existing")) {
				if (!verifyExistingUser(usernameFromClient, hashedPasswordFromClient)) {
					continue;
				}
			} else {
				System.out.println("Client did not follow protocol");
				respondFailure("login");
				clientInputScanner.close();
				System.exit(0);
				return "";
			}

			successful = true;

		} while(!successful);

		return hashedPasswordFromClient;
	}

	// Returns session key/initialization vector pair
	static GeneralHelper.SessionKeyIVPair handleSessionKeyExchange() {
		Scanner clientMessageScanner = new Scanner(clientInputStream);
		String clientMessage = clientMessageScanner.nextLine();

		// TODO important: decrypt client message with server private key!

		String messageHeader = "SessionKey:";
		int headerLength = messageHeader.length();
		if (clientMessage.startsWith(messageHeader)) {
			String messageBody = clientMessage.substring(messageHeader.length());
			int commaIndex = messageBody.indexOf(',');

			String sessionKeyHexString = messageBody.substring(0, commaIndex);
			String initializationVectorHexString = messageBody.substring(commaIndex + 1);
			System.out.println("Session key: " + sessionKeyHexString);
			System.out.println("IV: " + initializationVectorHexString);

			byte[] sessionKeyBytes = DatatypeConverter.parseHexBinary(sessionKeyHexString);
			SecretKey sessionKey = new SecretKeySpec(sessionKeyBytes, 0, sessionKeyBytes.length, "AES");

			byte[] initializationVector = DatatypeConverter.parseHexBinary(initializationVectorHexString);

			respondSuccess("sessionkey");

			return new GeneralHelper.SessionKeyIVPair(sessionKey, initializationVector);
		} else {
			System.out.println("Client did not follow protocol");
			respondFailure("sessionkey");
			//TODO: don't just exit? keep server up to try again?
			System.exit(0);
			return null;
		}
	}


	public static void main(String[] args) {
		HashMap<String, Boolean> modes = GeneralHelper.parseCommandLine(args);

		try {

			privateKey = SecurityHelper.storeKeyPair("server");

			// create server socket
			ServerSocket server = new ServerSocket(8080);
			server.setReuseAddress(true);
			System.out.println("Waiting for client...");

			ReadSocketThread receiveMessageThread = null;
			WriteSocketThread sendMessageThread = null;
			MessagingWindow messagingWindow = null;

			while (true) {
				try {
					// wait for client connection
					Socket clientConnection = server.accept();
					System.out.println("Client connected!");

					// create streams for the socket
					clientOutputStream = clientConnection.getOutputStream();
					clientInputStream = clientConnection.getInputStream();

					/*
					BufferedReader bRead = new BufferedReader(new InputStreamReader(clientInputStream));
					
					String line = bRead.readLine();

					String flag_strings[] = line.split(" ");

					if (modes.get("confidentiality") != Boolean.parseBoolean(flag_strings[0]) 
							|| modes.get("integrity") != Boolean.parseBoolean(flag_strings[1]) 
							|| modes.get("availability") != Boolean.parseBoolean(flag_strings[2])) {

						System.out.println("Client modes do not match Server modes; closing connection.");
						continue;
					}
					*/

					if(messagingWindow != null)
						messagingWindow.close();

					if(sendMessageThread != null)
						sendMessageThread.stop();

					if(receiveMessageThread != null)
						receiveMessageThread.stop();
					

					String hashedPasswordFromClient = handleUserLogin();
					if(hashedPasswordFromClient.equals(""))
						continue;

					GeneralHelper.SessionKeyIVPair sessionKeyIVPair = new GeneralHelper.SessionKeyIVPair(null, null);

					if (modes.get("confidentiality")) {
						sessionKeyIVPair = handleSessionKeyExchange();
					}

					messagingWindow = GeneralHelper.createUI();

					receiveMessageThread = new ReadSocketThread("receive-messages", 
							clientInputStream, modes, null, sessionKeyIVPair, messagingWindow);

					receiveMessageThread.start();

					sendMessageThread = new WriteSocketThread("send-messages",
						clientOutputStream, modes, null, sessionKeyIVPair, messagingWindow);

					sendMessageThread.start();

					if (modes.get("integrity") || modes.get("authentication")) {
						// Get client's public key
					}

					/*
					try {
						receiveMessageThread.join();
						sendMessageThread.join();
						System.out.println("Both threads joined??");
					} catch (InterruptedException e) {
						System.out.println(e);
						continue;
					} */
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