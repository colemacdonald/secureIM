//package secureIM;
/*
 * Class to include certain functions used by both client and server
 */
 
import java.io.*;
import java.io.BufferedReader;
import java.util.*;
import javax.crypto.*;

public class GeneralHelper
{
    static void printUsage()
    {
        System.out.println("Invalid use. Usage:\njava Server [-cian]");
        System.out.println("c enables confidentiality (asymmetric encryption)");
        System.out.println("i enables data integrity");
        System.out.println("a enables authentication");
        System.out.println("n indicates a new user");
        System.out.println("undocumented arguments will be ignored");
    }

    /* parse command line for c i a, store results hashmap */
    static HashMap<String, Boolean> parseCommandLine(String[] args) {
        HashMap<String, Boolean> modes = new HashMap<String, Boolean>();

        if (args.length < 0) {
            printUsage();
            System.exit(0);
        } else if (args.length == 0) {
            modes.put("authentication", false);
            modes.put("confidentiality", false);
            modes.put("integrity", false);
            modes.put("newUser", false);
        } else {
            modes.put("authentication", args[0].contains("a"));
            modes.put("confidentiality", args[0].contains("c"));
            modes.put("integrity", args[0].contains("i"));
            modes.put("newUser", args[0].contains("n"));
        }

        return modes;
    }

    static void safePrintln(String s) {
        synchronized(System.out) {
            System.out.println(s);
        }
    }

    static MessagingWindow createUI(String windowName) {
        StringBuffer userInputBuffer = new StringBuffer();
        MessagingWindow window = new MessagingWindow(userInputBuffer, windowName);

        return window;
    }
}