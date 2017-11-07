//package secureIM;
/*
 * Security-related helper methods used by both client and server
 */

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.*;
import java.util.*;
import java.security.*;
import java.security.spec.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;
import java.io.BufferedReader;



public class SecurityHelper {

    static final int DIGEST_LENGTH = 32;
    static final String PUBLIC_KEY_FILE = "shared_data/trusted_public_keys.csv"
;
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
     * Converts hex string to corresponding byte array
     * Taken from: https://stackoverflow.com/questions/140131/convert-a-string-representation-of-a-hex-dump-to-a-byte-array-using-java
     */
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    /*
     * Uses java.security.MessageDigest to compute a SHA-256 hash
     * Returns a string of hex characters
     *
     * TODO: Hash with a salt when hashing passwords?
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
    static boolean confirmDigest(byte[] received)
    {
        byte[] receivedDigest = Arrays.copyOfRange(received, received.length - DIGEST_LENGTH, received.length);
        byte[] message = Arrays.copyOfRange(received, 0, received.length - DIGEST_LENGTH);

        byte[] computedDigest = computeDigest(message);
        return Arrays.equals(computedDigest, receivedDigest);
    }

    static String prepareMessage(String message, HashMap<String, Boolean> modes, 
        SecretKey sessionKey, SecretKey privateKey, byte[] iv)
    {
        /* CONFIDENTIALITY */
        byte[] encryptedWithSession;

        if(modes.get("confidentiality")) {// encrypt with session key
           encryptedWithSession = encryptWithSessionKey(message.getBytes(), iv, sessionKey);
        } else {
            encryptedWithSession = message.getBytes();
        }

        /* INTEGRITY */
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try{
            byteStream.write(encryptedWithSession);

            if(modes.get("integrity")) { // compute and add checksum
                byteStream.write(computeDigest(encryptedWithSession));
            }
        }
        catch (IOException e)
        {
            System.out.println("Exception: " + e);
        }

        /* AUTHENTICATION / INTEGRITY */
        if(modes.get("authentication") || modes.get("integrity")) {// encrypt with private key

        }
        return bytesToHex(byteStream.toByteArray());
    }

    static String parseAndDecryptMessage(String encryptedMessage, HashMap<String, Boolean> modes, SecretKey sessionKey, SecretKey privateKey, byte[] iv)
    {
        byte[] encryptedBytes = hexStringToByteArray(encryptedMessage);
        if(modes.get("authentication")) // decrypt using public key
        {

        }

        byte[] justMessage;
        if(modes.get("integrity")) // seperate message from digest and compare
        {
            if(!confirmDigest(encryptedBytes))
            {
                System.out.println("Digest did not match. Likely tampered with.");
                //TODO: what to do?
            }
            justMessage = Arrays.copyOfRange(encryptedBytes, 0, encryptedBytes.length - DIGEST_LENGTH);
        } else {
            justMessage = encryptedBytes;
        }

        String plainText;
        if(modes.get("confidentiality")) // decrypt using session key
        {
            plainText = new String(decryptWithSessionKey(justMessage, iv, sessionKey));
        } else {
            plainText = new String(justMessage);
        }

        return plainText;
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


    static byte[] encryptWithPublicKey(String plaintext, PublicKey pubKey) throws Exception{
        
        Cipher pubCipher = Cipher.getInstance("RSA");
        pubCipher.init(Cipher.PUBLIC_KEY, pubKey);
        byte[] encryptedBytes = pubCipher.doFinal(plaintext.getBytes());

        return encryptedBytes;
    }


    static byte[] decryptWithPrivateKey(byte[] ciphertext, PrivateKey privKey) throws Exception{
        
        Cipher privCipher = Cipher.getInstance("RSA");
        privCipher.init(Cipher.PRIVATE_KEY, privKey);
        byte[] decryptedBytes = privCipher.doFinal(ciphertext);

        return decryptedBytes;
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

    static KeyPair generateUserKeyPair() {
        try{
            KeyPairGenerator keypg = KeyPairGenerator.getInstance("RSA");
            keypg.initialize(2048);
            KeyPair keyP = keypg.generateKeyPair();
            return keyP;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    static PrivateKey storeServerKeyPair() throws Exception{

        File serverKeyFile = new File("server_private_key.key");
        if (serverKeyFile.length() > 0){
            byte[] keyBytes = null;
            BufferedReader buffer = new BufferedReader(new FileReader("server_private_key.key"));
            keyBytes = buffer.readLine().getBytes();
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(keyBytes);
            PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
            return privateKey;
        } 
        else {            
            KeyPair serverKP = generateUserKeyPair();
            Base64.Encoder encoder = Base64.getEncoder();

            Writer keyFile = new FileWriter("server_private_key.key");
            keyFile.write(encoder.encodeToString(serverKP.getPrivate().getEncoded()));
            keyFile.close();
            keyFile = new FileWriter(PUBLIC_KEY_FILE);
            keyFile.write("server," + encoder.encodeToString(serverKP.getPublic().getEncoded()));
            keyFile.close();
            return serverKP.getPrivate();          
        }
    }


    static PrivateKey storeClientKeyPair(String userName) throws Exception{

        File clientKeyFile = new File(userName + "_private_key.key");

        if (clientKeyFile.length() > 0){
            byte[] keyBytes = null;
            BufferedReader buffer = new BufferedReader(new FileReader(userName + "_private_key.key"));
            keyBytes = buffer.readLine().getBytes();
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(keyBytes);
            PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
            return privateKey;
        }
        else {
            KeyPair clientKP = SecurityHelper.generateUserKeyPair();
            Base64.Encoder encoder = Base64.getEncoder();

            Writer keyFile = new FileWriter(userName + "_private_key.key");
            keyFile.write(userName + "," + encoder.encodeToString(clientKP.getPrivate().getEncoded()));
            keyFile.close();
            keyFile = new FileWriter(PUBLIC_KEY_FILE);
            keyFile.write(userName + "," + encoder.encodeToString(clientKP.getPublic().getEncoded()));
            keyFile.close();
            return clientKP.getPrivate();
        }
    }


    static PublicKey getServerPublicKey() throws Exception{
        BufferedReader keys = new BufferedReader(new FileReader("shared_data/trusted_public_keys.csv"));
        return null;
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

        HashMap<String, Boolean> modes = new HashMap<String, Boolean>();

        modes.put("authentication", true);
        modes.put("confidentiality", true);
        modes.put("integrity", true);

        String msg = prepareMessage(plaintext, modes, sessionKey, sessionKey, initializationVector);
        System.out.println("MESSAGE: " + msg);
        String msg2 = parseAndDecryptMessage(msg, modes, sessionKey, sessionKey, initializationVector);
        System.out.println("DECRYPTED: " + msg2);

        byte[] ciphertext = encryptWithSessionKey(plaintext.getBytes(), initializationVector, sessionKey);
        System.out.println("ciphertext: " + Arrays.toString(ciphertext));

        byte[] decryptedText = decryptWithSessionKey(ciphertext, initializationVector, sessionKey);
        String decryptedString = new String(decryptedText);
        System.out.println("decrypted text: " + decryptedString);
    }
}