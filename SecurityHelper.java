//package secureIM;
/*
 * Security-related helper methods used by both client and server
 */

import java.io.*;
import java.lang.*;
import java.util.*;
import java.security.*;
import java.security.spec.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import javax.xml.bind.DatatypeConverter;
import java.security.cert.CertificateFactory;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.Path;

public class SecurityHelper {

    static final int DIGEST_LENGTH = 32;
    static final String PUBLIC_KEY_FILE = "shared_data/trusted_public_keys.csv";
    static final String USER_PASS_FILE = "shared_data/user_hashed_passwords.csv";

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

    static class SessionKeyIVPair {
        public SecretKey sessionKey;
        public byte[] initializationVector;

        public SessionKeyIVPair(SecretKey sessionKey, byte[] initializationVector) {
            this.sessionKey = sessionKey;
            this.initializationVector = initializationVector;
        }
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
     */
    static boolean confirmDigest(byte[] received)
    {
        byte[] receivedDigest = Arrays.copyOfRange(received, received.length - DIGEST_LENGTH, received.length);
        byte[] message = Arrays.copyOfRange(received, 0, received.length - DIGEST_LENGTH);

        byte[] computedDigest = computeDigest(message);
        return Arrays.equals(computedDigest, receivedDigest);
    }

    static String prepareMessage(String message, HashMap<String, Boolean> modes, 
        SecretKey sessionKey, Key privateKey, byte[] iv)
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
        try {
            byteStream.write(encryptedWithSession);

            if(modes.get("integrity")) { // compute and add checksum
                byteStream.write(computeDigest(encryptedWithSession));
            }

            encryptedWithSession = byteStream.toByteArray();

            /* AUTHENTICATION / INTEGRITY */
            if(modes.get("authentication") || modes.get("integrity")) {// encrypt with private key
                encryptedWithSession = Base64.getDecoder().decode(encryptAssymetric(new String(Base64.getEncoder().encodeToString(encryptedWithSession)), privateKey));
            }
        }
        catch (IOException e)
        {
            System.out.println("Exception: " + e);
        }
        return bytesToHex(encryptedWithSession);
    }

    static String parseAndDecryptMessage(String encryptedMessage, HashMap<String, Boolean> modes, SecretKey sessionKey, Key publicKey, byte[] iv)
    {   
        if(modes.get("authentication")) // decrypt using public key
        {
            encryptedMessage = decryptAssymetric(Base64.getEncoder().encodeToString(hexStringToByteArray(encryptedMessage)), publicKey);
        }

        byte[] encryptedBytes = hexStringToByteArray(encryptedMessage);
        byte[] justMessage;
        if(modes.get("integrity")) // seperate message from digest and compare
        {
            if(!confirmDigest(encryptedBytes))
            {
                GeneralHelper.safePrintln("Digest did not match. Likely tampered with.");
                return "";
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

    static KeyPair generateUserKeyPair() {
        try{
            KeyPairGenerator keypg = KeyPairGenerator.getInstance("RSA");
            SecureRandom random = new SecureRandom();

            keypg.initialize(2048, random);

            KeyPair keyP = keypg.generateKeyPair();

            return keyP;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    static String encryptAssymetric(String plaintext, Key key) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            
            String encryptedData = Base64.getEncoder().encodeToString(cipher.doFinal(plaintext.getBytes()));

            return encryptedData;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    static String decryptAssymetric(String ciphertext, Key key) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, key);
            
            //System.out.println("ciphertext: " + Base64.getEncoder().encode(hexStringToByteArray(ciphertext)));
            String decryptedData = new String(cipher.doFinal(Base64.getDecoder().decode(ciphertext)));

            return decryptedData;

        } catch (Exception e) {
            e.printStackTrace();
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

    static byte[] signWithPrivateKey(PrivateKey privateKey, byte[] plaintext) {
        try{
            Signature rsa = Signature.getInstance("RSA");
            rsa.initSign(privateKey);
            rsa.update(plaintext);
            byte[] sig = rsa.sign();
            return sig;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Stores a key pair for the given user and returns the private key. 
    // If user already has a stored key pair, returns the existing private key.
    static PrivateKey storeKeyPair(String userName) throws Exception{

        File clientKeyFile = new File(userName + "_private_key.key");
        PublicKey userPublicKey = getUserPublicKey(userName);

        // generate and store keypair
        if (clientKeyFile.length() == 0 || userPublicKey == null) {
            System.out.println("Generating public/private keypair, this may take a moment...");

            KeyPair clientKP = SecurityHelper.generateUserKeyPair();
            Base64.Encoder encoder = Base64.getEncoder();

            Writer keyFile = new FileWriter(userName + "_private_key.key", false);
            keyFile.write(encoder.encodeToString(clientKP.getPrivate().getEncoded()));
            keyFile.close();

            keyFile = new FileWriter(PUBLIC_KEY_FILE, true);
            keyFile.write(userName + "," + encoder.encodeToString(clientKP.getPublic().getEncoded()) + "\n");
            keyFile.close();

            System.out.println("Keypair generated and stored");
            return clientKP.getPrivate();
        }
        // read private key from file, because it exists already
        else {            
            byte[] keyBytes = null;

            Base64.Decoder decoder = Base64.getDecoder();

            Path path = Paths.get(userName + "_private_key.key");
            keyBytes = Files.readAllBytes(path);

            PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(decoder.decode(keyBytes));
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = kf.generatePrivate(ks);
            return privateKey;
        }
    }

    // Searches through shared user password hash file and return whether or not provided username already exists
    static boolean userExists(String userName) {

        try {
            BufferedReader br = new BufferedReader(new FileReader(USER_PASS_FILE));
            String line = br.readLine();
            String[] savedName;
            while (line != null) {
                savedName = line.split(",");
                if (savedName[0].equals(userName)) {
                    return true;
                }
                line = br.readLine();
            }
            return false;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Returns given username's public key from shared key-store
    static PublicKey getUserPublicKey(String userName) {
        try {
            BufferedReader keys = new BufferedReader(new FileReader(PUBLIC_KEY_FILE));
            String line = keys.readLine();
            while(line != null) {
                if (line.startsWith(userName)){
                    String[] serverKey = line.split(",");
                    byte[] keyBytes = serverKey[1].getBytes();
                    Base64.Decoder decoder = Base64.getDecoder();
                    X509EncodedKeySpec ks = new X509EncodedKeySpec(decoder.decode(keyBytes));
                    KeyFactory kf = KeyFactory.getInstance("RSA");
                    PublicKey publicKey = kf.generatePublic(ks);
                    return publicKey;
                }

                line = keys.readLine();
            }
            return null;
        } catch (Exception e) {
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