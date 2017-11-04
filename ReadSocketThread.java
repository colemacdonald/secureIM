import java.net.*;
import java.security.*;
import javax.crypto.*;
import java.io.*;
import java.util.Arrays;
import java.util.Scanner;
import java.lang.Thread;
import java.lang.StringBuffer;
import java.util.Date;

/*
 * Thread that monitors socket for incoming messages
 */

class ReadSocketThread implements Runnable
{
    private Thread t;
    private String threadName;
    private StringBuffer inputBuffer;
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
                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Date date = new Date();
                System.out.println(dateFormat.format(date) + " - " + msg);
            }
        }   
    }

    public void start () 
    {
        System.out.println("Starting " +  threadName );
        if(t == null) 
        {
            t = new Thread(this, threadName);
            t.start();
        }
    }
}