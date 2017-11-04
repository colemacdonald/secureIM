import java.security.*;
import javax.crypto.*;

public class SecurityHelper {

    /*
     * Converts a byte array into a hex string
     * Taken from: https://stackoverflow.com/questions/15429257/how-to-convert-byte-array-to-hexstring-in-java
     */
    static String bytesToHex(byte[] in) 
    {
		final StringBuilder builder = new StringBuilder();
		for(byte b : in) {
			builder.append(String.format("%02x", b));
		}
		return builder.toString();
	}

    /*
     * Uses java.security.MessageDigest to compute a SHA-256 hash
     * Returns a string of hex characters
     */
	static String computeDigest(String message)
	{
		String ret = "";
		try 
        {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(message.getBytes());
			byte[] digest = md.digest();
			ret = bytesToHex(digest);
		}
		catch (Exception e) 
        {
			System.out.println("Exception: " + e);
		}
		return ret;
	}

    /*
     * To be used when receiveing a message - check received digest against computed one
     * TODO: Will the digest have been seperated from the msg at this point?
     */
    static boolean confirmDigest(String message, String receivedDigest)
    {
        String computedDigest = computeDigest(message);
        return computedDigest.equals(receivedDigest);
    }
}