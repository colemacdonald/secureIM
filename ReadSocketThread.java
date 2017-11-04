//package secureIM;

import java.net.*;
import java.io.*;
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

    ReadSocketThread(String _threadName, InputStream _inStream)
    {
        this.inStream = _inStream;
        this.threadName = _threadName;
    }

    public void run()
    {
        Scanner msgIn = new Scanner(inStream);
        for(;;)
        {
            while(!msgIn.hasNext());

            if(msgIn.hasNext())
            {
                String msg = msgIn.next();
                String plainMsg = SecurityHelper.parseMessage(msg, true, true, true);
                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Date date = new Date();
                GeneralHelper.safePrintln(dateFormat.format(date) + " - " + plainMsg);
            }
        }   
    }

    public void start () 
    {
        GeneralHelper.safePrintln("Starting " +  threadName );
        if(t == null) 
        {
            t = new Thread(this, threadName);
            t.start();
        }
    }
}