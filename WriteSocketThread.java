//package secureIM;

import java.net.*;
import java.security.*;
import javax.crypto.*;
import java.io.*;
import java.util.*;
import java.lang.Thread;
import java.lang.StringBuffer;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.DateFormat;


/*
 * Thread that takes in user input and appends to the provided StringBuffer
 */

class WriteSocketThread implements Runnable
{
    private Thread t;
    private String threadName;
    private OutputStream outputStream;
    private HashMap<String, Boolean> modes;
    private SecretKey sessionKey;
    private SecretKey privateKey;
    private byte[] iv;
    private MessagingWindow messagingWindow;

    WriteSocketThread(String threadName, OutputStream outputStream, HashMap<String, Boolean> modes,
        SecretKey privateKey, SecurityHelper.SessionKeyIVPair sessionKeyIVPair, MessagingWindow window)
    {
        this.outputStream = outputStream;
        this.threadName = threadName;
        this.modes = modes;
        this.sessionKey = sessionKeyIVPair.sessionKey;
        this.privateKey = privateKey;
        this.iv = sessionKeyIVPair.initializationVector;
        this.messagingWindow = window;
    }

    public void run()
    {
        //Object inputReady = new Object();
        StringBuffer userInputBuffer = messagingWindow.getUserInputBuffer();

        PrintStream socketWrite = new PrintStream(outputStream);

        while(!Thread.currentThread().isInterrupted())
        {
            // wait on input (without wasting CPU time)
            //synchronized(inputReady) {
               // while (userInputBuffer.length() == 0) {
                    //try {
                        // This is signalled within UserInput.java
                        //inputReady.wait();

                        if(userInputBuffer.length() > 0) {
                            String plaintext;

                            synchronized(userInputBuffer) {
                                plaintext = userInputBuffer.toString();
                                userInputBuffer.setLength(0);                                
                            }

                            String msg = SecurityHelper.prepareMessage(plaintext, modes, 
                                sessionKey, privateKey, iv);

                            //GeneralHelper.safePrintln("Sending: " + msg + " - " + SecurityHelper.parseAndDecryptMessage(msg, modes, sessionKey, sessionKey, iv));

                            socketWrite.println(msg);
                            socketWrite.flush();
                            
                            // DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                            // Date date = new Date();
                            // GeneralHelper.safePrintln("> " + dateFormat.format(date) + " - " + plaintext);                       
                        }
                    //} catch (InterruptedException e) {
                    //    e.printStackTrace();
                    //}
                //}
            //}
        }
    }

    public void stop()
    {
        Thread.currentThread().interrupt();
    }

    public void start() 
    {
        System.out.println("Starting " +  threadName );
        if(t == null) 
        {
            t = new Thread(this, threadName);
            t.start();
        }
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