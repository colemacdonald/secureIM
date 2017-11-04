//package secureIM;
/*
 * Security-related helper methods used by both client and server
 */

import java.security.*;
import javax.crypto.*;
import java.lang.*;

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
	static byte[] computeDigest(byte[] message)
	{
		byte[] digest = {};
		try 
        {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(message);
			digest = md.digest();
		}
		catch (Exception e) 
        {
			GeneralHelper.safePrintln("Exception: " + e);
		}
		return digest;
	}

    /*
     * To be used when receiveing a message - check received digest against computed one
     * TODO: Will the digest have been seperated from the msg at this point?
     */
    static boolean confirmDigest(String message, String receivedDigest)
    {
        //String computedDigest = computeDigest(message);
        return true; //computedDigest.equals(receivedDigest);
    }

    static String prepareMessage(String message, boolean confidential, boolean integrity, boolean authenticate)
    {
        StringBuilder ret = new StringBuilder();

        byte[] encryptedSession;

        if(confidential) // encrypt with session key
        {
           // encryptedSession = encryptWithSessionKey(message.getBytes(), );
        }
        else
        {
            encryptedSession = message.getBytes();
        }

        if(integrity) // compute and add checksum
        {
            
        }
        ret.append(message);

        if(authenticate || integrity) // encrypt with private key
        {

        }

        return ret.toString();
    }

    static String parseMessage(String encryptedMessage, boolean confidential, boolean integrity, boolean authenticate)
    {
        if(authenticate) // decrypt using public key
        {

        }

        if(integrity) // seperate message from digest and compare
        {

        }

        if(confidential) // decrypt using session key
        {
        
        }

        return "";
    }

    static String encryptWithSessionKey(String sessionKey) {


        return "";
    }
}