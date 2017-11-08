//package secureIM;

import java.net.*;
import java.security.*;
import javax.crypto.*;
import java.io.*;
import java.util.*;
import java.util.Arrays;
import java.util.Scanner;
import java.lang.Thread;
import java.lang.StringBuffer;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.DateFormat;

/*
 * Thread that monitors socket for incoming messages
 */

class ReadSocketThread implements Runnable
{
    private Thread t;
    private String threadName;
    private InputStream inStream;
    private HashMap<String, Boolean> modes;
    private SecretKey sessionKey;
    private SecretKey privateKey;
    private byte[] iv;
    private MessagingWindow messagingWindow;

    ReadSocketThread(String threadName, InputStream inStream, HashMap<String, Boolean> modes, 
        SecretKey privateKey, GeneralHelper.SessionKeyIVPair sessionKeyIVPair, MessagingWindow window)
    {
        this.inStream = inStream;
        this.threadName = threadName;
        this.modes = modes;
        this.sessionKey = sessionKeyIVPair.sessionKey;
        this.privateKey = privateKey;
        this.iv = sessionKeyIVPair.initializationVector;
        this.messagingWindow = window;
    }

    public void run()
    {
        Scanner msgIn = new Scanner(inStream);

        while(!Thread.currentThread().isInterrupted())
        {
            if(!msgIn.hasNextLine())
                continue;

            String msg = msgIn.nextLine();
            String plainMsg = SecurityHelper.parseAndDecryptMessage(msg, modes, sessionKey, privateKey, iv);
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            messagingWindow.writeToMessageWindow(dateFormat.format(date) + " < " + plainMsg);
        }
    }

    public void start() 
    {
        GeneralHelper.safePrintln("Starting " +  threadName );
        if(t == null) 
        {
            t = new Thread(this, threadName);
            t.start();
        }
    }

    public void stop()
    {
        Thread.currentThread().interrupt();
    }

    public void join() throws InterruptedException
    {
        if (t != null)
        {
            try {
                t.join();
            } catch (InterruptedException e) {
                throw e;
            }
        }
    }
}