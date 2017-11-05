//package secureIM;

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
    private StringBuffer inputBuffer;

    WriteSocketThread(String _threadName, OutputStream _outputStream)
    {
        this.outputStream = _outputStream;
        this.threadName = _threadName;
    }

    public void run()
    {
        inputBuffer = new StringBuffer();
        Scanner userInput = new Scanner(System.in);
        UserInput ui = new UserInput(inputBuffer);
        ui.CreateTextField();

        while(true)
        {
            // wait on input
            GeneralHelper.safePrintln("> ");
            while(inputBuffer.length() == 0);

            //inputBuffer.append("\n" + userInput.nextLine());
            GeneralHelper.safePrintln("Thread sees: " + inputBuffer.toString());

            inputBuffer.setLength(0);
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