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

	static void respondSuccess() {
		PrintStream outputToClient = new PrintStream(clientOutputStream, true);
		outputToClient.println("Success");
	}

	static void respondFailure() {
		PrintStream outputToClient = new PrintStream(clientOutputStream, true);
		outputToClient.println("Failure");
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
			//TODO: check if the username already exists
			FileWriter passwordWriter = new FileWriter("shared_data/user_hashed_passwords.csv", true);
			passwordWriter.write(username + "," + passwordHash + "\n");
			passwordWriter.close();

			respondSuccess();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			respondFailure();
			System.exit(0);
			return false;
		}
	}

	static boolean verifyExistingUser(String username, String passwordHash) {
		try {
			String passwordFile = "shared_data/user_hashed_passwords.csv";
			BufferedReader sessionKeyReader = new BufferedReader(new FileReader(passwordFile));

			String line = sessionKeyReader.readLine();
			while (line != null) {
				String[] entries = line.split(",");

				if (entries[0].equals(username)) {
					if (entries[1].equals(passwordHash)) {
						System.out.println("User " + username + " logged in succesfully");
						respondSuccess();
						return true;
					} else {
						System.out.println("Incorrect password from Client!");
						respondFailure();
						return false;
					}
				}
			}

			System.out.println("User does not exist!");
			respondFailure();
			return false;

		} catch (IOException e) {
			e.printStackTrace();
			respondFailure();
			System.exit(0);
			return false;
		}
	}

	// Returns hashed password, or empty string if their login failed (wrong password, etc)
	static String handleUserLogin() {
		Scanner clientInputScanner = new Scanner(clientInputStream);
		String newOrExisting = clientInputScanner.nextLine();
		String usernameFromClient = clientInputScanner.nextLine();
		String hashedPasswordFromClient = clientInputScanner.nextLine();

		if (!validateClientInput(newOrExisting, usernameFromClient, hashedPasswordFromClient)) {
			System.out.println("Client did not follow correct protocol, exiting");
			System.exit(0);
		}

		usernameFromClient = usernameFromClient.substring("Username:".length());
		hashedPasswordFromClient = hashedPasswordFromClient.substring("Password:".length());

		if (newOrExisting.equals("New")) {
			if (!addNewUser(usernameFromClient, hashedPasswordFromClient)) {
				return "";
			}
		} else if (newOrExisting.equals("Existing")) {
			if (!verifyExistingUser(usernameFromClient, hashedPasswordFromClient)) {
				return "";
			}
		} else {
			System.out.println("Client did not follow protocol");
			respondFailure();
			System.exit(0);
		}

		// TODO: deal with failed user login/signup, on server side
		return hashedPasswordFromClient;
	}

	public static void main(String[] args) {
		HashMap<String, Boolean> modes = GeneralHelper.parseCommandLine(args);
		
		try {
			ServerSocket server = new ServerSocket(8080);
			server.setReuseAddress(true);
			System.out.println("Waiting for client...");

			while (true) {
				try {
					Socket clientConnection = server.accept();
					System.out.println("Client connected!");

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

					String hashedPasswordFromClient = "";
					while (hashedPasswordFromClient.length() == 0) {
						hashedPasswordFromClient = handleUserLogin();
					}

					if (modes.get("confidentiality")) {
								
					}

					if (modes.get("integrity") || modes.get("authentication")) {
						// Get client's public key
					}

					/* TESTING MSG SEND */

					//String password = "password";
					SecureRandom random = new SecureRandom();
					byte[] initializationVector = {-18, 8, -18, -62, -95, -64, 36, -17, -67, 67, 87, 25, -18, -15, -38, 81};//new byte[16];
					//random.nextBytes(initializationVector);

					System.out.println(Arrays.toString(initializationVector));
			
					SecretKey sessionKey = SecurityHelper.generatePasswordBasedKey(hashedPasswordFromClient);
					// String encodedKey = Base64.getEncoder().encodeToString(sessionKey.getEncoded());
					// System.out.println(encodedKey);

					//byte[] decodedKey = Base64.getDecoder().decode("B0FZlSHiUEKsInRxJCJwm7yXXy7MpcVpX6yCxBGjrCw=");
					// rebuild key using SecretKeySpec
					//SecretKey sessionKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");


					ReadSocketThread receiveMessageThread = new ReadSocketThread("receive-messages", 
						clientInputStream, modes, sessionKey, sessionKey, initializationVector);
					receiveMessageThread.start();

					WriteSocketThread sendMessageThread = new WriteSocketThread("send-messages",
						clientOutputStream, modes, sessionKey, sessionKey, initializationVector);
					sendMessageThread.start();
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
			}
		} catch (java.net.SocketException e) {
			System.out.println("SocketException: " + e);
			System.exit(0);
		} catch (IOException e) {
			System.out.println("IOException: " + e);
			System.exit(0);
		}		
	}
}