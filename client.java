/*
Implement client functionality as per assignment spec

*/
import java.net.*;
import java.security.*;
import javax.crypto.*;

public class Client {
	public static void main(String[] args) {
		try {
			Socket serverConnection = new Socket("localhost", 8080);
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		}
	}
}