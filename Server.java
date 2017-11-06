//package secureIM;
/*
 * Implement server functionality as per assignment spec
 */

import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.*;
import javax.xml.bind.DatatypeConverter;


public class Server {

	private static StringBuffer inputBuffer;// = StringBuffer();

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

					OutputStream userInputStream = clientConnection.getOutputStream();
					InputStream clientInputStream = clientConnection.getInputStream();

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

					System.out.println("Message 1: " + newOrExisting);
					System.out.println("Message 2: " + usernameFromClient);
					System.out.println("Message 3: " + hashedPasswordFromClient);

					if (modes.get("confidentiality")) {
						if (newOrExisting.equals("New")) {
							//TODO: check if the username already exists
							try {
								FileWriter passwordWriter = new FileWriter("shared_data/user_hashed_passwords.csv", true);
								passwordWriter.write(usernameFromClient + "," + hashedPasswordFromClient + "\n");
								passwordWriter.close();
							} catch (IOException e) {
								e.printStackTrace();
								System.exit(0);
							}
						} else if (newOrExisting.equals("Existing")) {
							// Verify session key
							try {
								String passwordFile = "shared_data/user_hashed_passwords.csv";
								BufferedReader sessionKeyReader = new BufferedReader(new FileReader(passwordFile));

								String line = sessionKeyReader.readLine();
								while (line != null) {
									String[] entries = line.split(",");

									if (entries[0].equals(usernameFromClient)) {
										if (entries[1] != hashedPasswordFromClient) {
											// TODO: send this to the Client
											System.out.println("Incorrect password from Client!");
										} else {
											System.out.println("User " + usernameFromClient + " logged in succesfully");
										}

										break;
									}
								}

								if (line == null) {
									// TODO: send this to the Client
									System.out.println("User does not exist!");
								}
							} catch (IOException e) {
								e.printStackTrace();
								System.exit(0);
							}
						} else {
							System.out.println("Client did not follow protocol, exiting");
							System.exit(0);
						}				
					}

					if (modes.get("integrity") || modes.get("authentication")) {
						// Get client's public key
					}

					ReadSocketThread receiveMessageThread = new ReadSocketThread("receive-messages", clientInputStream, modes);
					receiveMessageThread.start();

					WriteSocketThread sendMessageThread = new WriteSocketThread("send-messages", userInputStream, modes);
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