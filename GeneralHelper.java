/*
 * Class to include certain functions used by both client and server
 */
 
import java.net.*;
import java.security.*;
import javax.crypto.*;
import java.io.*;
import java.util.Arrays;
import java.util.HashMap;

public class Helper
{

    static void printUsage()
    {
        System.out.println("Invalid use. Usage:\njava Server [-cia]");
        System.out.println("c enables encryption, i enables data integrity, a enables authentication");
        System.out.println("undocumented arguments will be ignored");
    }

    /* parse command line for c i a, store results hashmap */
    static HashMap<String, Boolean> parseCommandLine(String[] args)
    {
        HashMap<String, Boolean> modes = new HashMap<String, Boolean>();

        if (args.length < 0) {
            printUsage();
            System.exit(0);
        } else if (args.length == 0) {
            modes.put("availability", false);
            modes.put("confidentiality", false);
            modes.put("integrity", false);
        } else {
            modes.put("availability", args[0].contains("a"));
            modes.put("confidentiality", args[0].contains("c"));
            modes.put("integrity", args[0].contains("i"));
        }

        return modes;
    }
}