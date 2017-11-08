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
    SecretKey sessionKey;
    SecretKey privateKey;
    byte[] iv;

    ReadSocketThread(String _threadName, InputStream _inStream, HashMap<String, Boolean> _modes, 
        SecretKey _privateKey, GeneralHelper.SessionKeyIVPair sessionKeyIVPair)
    {
        this.inStream = _inStream;
        this.threadName = _threadName;
        this.modes = _modes;
        this.sessionKey = sessionKeyIVPair.sessionKey;
        this.privateKey = _privateKey;
        this.iv = sessionKeyIVPair.initializationVector;
    }

    public void run()
    {
        Scanner msgIn = new Scanner(inStream);

        while(true)
        {
            while(!msgIn.hasNext());

            if(msgIn.hasNext())
            {
                String msg = msgIn.next();
                //GeneralHelper.safePrintln("Received: " + msg);
                String plainMsg = SecurityHelper.parseAndDecryptMessage(msg, modes, sessionKey, privateKey, iv);
                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Date date = new Date();
                GeneralHelper.safePrintln("< " + dateFormat.format(date) + " - " + plainMsg);
            }
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