//package secureIM;
/*
 * Security-related helper methods used by both client and server
 */

import java.lang.*;
import java.util.*;
import java.security.*;
import java.security.spec.*;
import javax.crypto.*;
import javax.crypto.spec.*;

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

    static byte[] encryptWithSessionKey(byte[] plaintext, byte[] initializationVector, SecretKey sessionKey) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initializationVector);
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");

            c.init(Cipher.ENCRYPT_MODE, sessionKey, iv);

            byte[] cipherText = c.doFinal(plaintext);
        
            return cipherText;
            
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
            return null;
        }
    }

    static byte[] decryptWithSessionKey(byte[] ciphertext, byte[] initializationVector, SecretKey sessionKey) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initializationVector);
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");

            c.init(Cipher.DECRYPT_MODE, sessionKey, iv);

            byte[] cipherText = c.doFinal(ciphertext);
        
            return cipherText;
            
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
            return null;
        }
    }

    static SecretKey generatePasswordBasedKey(String password) {
        try {
            char[] passwordChars = password.toCharArray();

            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);

            KeySpec passwordBasedKey = new PBEKeySpec(passwordChars, salt, 65536, 256);

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            SecretKey tmp = factory.generateSecret(passwordBasedKey);

            SecretKey finalKey = new SecretKeySpec(tmp.getEncoded(), "AES");
            return finalKey;

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
            return null;
        }
    }

    public static void main(String[] args) {
        String password = "cailan";
        System.out.println("Password: " + password);
        
        SecureRandom random = new SecureRandom();
        byte[] initializationVector = new byte[16];
        random.nextBytes(initializationVector);

        SecretKey sessionKey = generatePasswordBasedKey(password);
        System.out.println("Key: " + Arrays.toString(sessionKey.getEncoded()));

        String plaintext = "hello, world";
        System.out.println("plaintext: " + plaintext);

        byte[] ciphertext = encryptWithSessionKey(plaintext.getBytes(), initializationVector, sessionKey);
        System.out.println("ciphertext: " + Arrays.toString(ciphertext));

        byte[] decryptedText = decryptWithSessionKey(ciphertext, initializationVector, sessionKey);
        String decryptedString = new String(decryptedText);
        System.out.println("decrypted text: " + decryptedString);
    }
}