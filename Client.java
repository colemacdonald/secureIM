//package secureIM;

/*
 * Implement client functionality as per assignment spec
 */

import java.net.*;
import java.io.*;
import java.util.*;

public class Client {

	static void promptLogin(boolean newUser) {
		Scanner sc = new Scanner(System.in);

		if (newUser) {
			boolean successful = false;

			while (!successful) {
				System.out.print("Please enter a username: ");
				String username = sc.nextLine();

				String plaintextPassword;

				Console console = System.console();

				if (console == null) {
					System.out.println("WARNING: Your console application does not support concealing password fields!!!");
					System.out.print("Please enter a password: ");
					plaintextPassword = sc.nextLine();
				} else {
					char[] password1 = console.readPassword("Please enter a password: ");
					char[] password2 = console.readPassword("Please repeat your password: ");
					if (!Arrays.equals(password1, password2)) {
						System.out.println("Passwords did not match, please try again");
						continue;
					}

					plaintextPassword = String.valueOf(password1);
				}

				byte[] passwordHash = SecurityHelper.computeDigest(plaintextPassword.getBytes());
				Formatter formatter = new Formatter();
				for (byte b : passwordHash) {
					formatter.format("%02x", b);
				}
				String passwordHashString = formatter.toString();

				try {
					FileWriter passwordWriter = new FileWriter("shared_data/user_hashed_passwords.csv", true);
					passwordWriter.write(username + "," + passwordHashString + "\n");
					passwordWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(0);
				}

				successful = true;
			}
		} else {
			System.out.println("Returning users not implemented yet (use the -n flag to add a new user)");
		}
	}

	public static void main(String[] args) {
		HashMap<String, Boolean> modes = GeneralHelper.parseCommandLine(args);		

		try {
			promptLogin(modes.get("newUser"));

			Socket serverConnection = new Socket("localhost", 8080);
			OutputStream outStream = serverConnection.getOutputStream();
			InputStream inStream = serverConnection.getInputStream();

			PrintStream printStream = new PrintStream(outStream, true);
			printStream.println("testing send");

			ReadSocketThread receiveMessageThread = new ReadSocketThread("client-read", inStream, modes);

			WriteSocketThread sendMessageThread = new WriteSocketThread("client-write", outStream);

			while(true);
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		}
	}
}