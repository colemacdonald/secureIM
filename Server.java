/*
Implement server functionality as per assignment spec

*/
import java.net.*;
import java.security.*;
import javax.crypto.*;

public class Server {

	private static boolean c = false;
	private static boolean i = false;
	private static boolean a = false;

	static void printUsage()
	{
		System.out.println("Invalid use. Usage:\njavac Server [-cia]");
		System.out.println("c enables encryption, i enables data integrity, a enables authentication");
		System.out.println("undocumented arguments will be ignored");
	}

	/* parse command line for c i a, store results in boolean values c i a */
	static void parseCommandLine(String[] args)
	{
		if(args.length < 0) {
			printUsage();
			System.exit(0);
		}

		if(args[0].contains("a"))
			a = true;
		if(args[0].contains("c"))
			c = true;
		if(args[0].contains("i"))
			i = true;
	}

	public static void main(String[] args) {
		parseCommandLine(args);

		
		try {

			ServerSocket server = new ServerSocket(8080);
			System.out.println("Waiting for client...");
			Socket clientConnection = server.accept();
			System.out.println("Client connected!");					


		} catch (Exception e) {
			System.out.println("Exception: " + e);
		}
	}
}