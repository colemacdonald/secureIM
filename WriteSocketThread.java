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
    private StringBuffer inputBuffer;
    private HashMap<String, Boolean> modes;
    SecretKey sessionKey;
    SecretKey privateKey;
    byte[] iv;

    WriteSocketThread(String _threadName, OutputStream _outputStream, HashMap<String, Boolean> _modes,
        SecretKey _sessionKey, SecretKey _privateKey, byte[] _iv)
    {
        this.outputStream = _outputStream;
        this.threadName = _threadName;
        this.modes = _modes;
        this.sessionKey = _sessionKey;
        this.privateKey = _privateKey;
        this.iv = _iv;
    }

    public void run()
    {
        inputBuffer = new StringBuffer();

        Object inputReady = new Object();
        UserInput ui = new UserInput(inputBuffer, inputReady);
        ui.CreateTextField();

        PrintStream socketWrite = new PrintStream(outputStream);

        while(true)
        {
            // wait on input (without wasting CPU time)
            synchronized(inputReady) {
                while (inputBuffer.length() == 0) {
                    try {
                        // This is signalled within UserInput.java
                        inputReady.wait();
                        if(inputBuffer.length() > 0) {
                            String plaintext = inputBuffer.toString();
                            inputBuffer.setLength(0);
                            String msg = SecurityHelper.prepareMessage(plaintext, modes, 
                                sessionKey, sessionKey, iv);

                            GeneralHelper.safePrintln("Sending: " + msg + " - " + SecurityHelper.parseAndDecryptMessage(msg, modes, sessionKey, sessionKey, iv));
                            
                            socketWrite.println(msg);
                            socketWrite.flush();
                            
                            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                            Date date = new Date();
                            GeneralHelper.safePrintln("> " + dateFormat.format(date) + " - " + plaintext);                       

                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
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