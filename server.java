/*
Implement server functionality as per assignment spec

*/
import java.net.*;
import java.security.*;
import javax.crypto.*;

public class Server {
	
	public static void main(String[] args) {
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