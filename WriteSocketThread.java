import java.net.*;
import java.security.*;
import javax.crypto.*;
import java.io.*;
import java.util.Arrays;
import java.util.Scanner;
import java.lang.Thread;
import java.lang.StringBuffer;

/*
 * Thread that takes in user input and appends to the provided StringBuffer
 */

class WriteSocketThread implements Runnable
{
    private Thread t;
    private String threadName;
    private OutputStream outputStream;

    WriteSocketThread(String _threadName, OutputStream _outputStream)
    {
        this.outputStream = _outputStream;
        this.threadName = _threadName;
    }

    public void run()
    {
        for(;;)
        {
            Scanner userInput = new Scanner(System.in);

            // wait on input
            GeneralHelper.safePrintln("> ");
            while(!userInput.hasNextLine());

            // if(userInput.hasNextLine())
            // {
            //     inputBuffer.append("\n" + userInput.nextLine());
            //     safePrint("Thread sees: " + inputBuffer.toString());
            // }
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
}