import java.net.*;
import java.security.*;
import javax.crypto.*;
import java.io.*;
import java.util.Arrays;
import java.util.Scanner;
import java.lang.Thread;
import java.lang.StringBuffer;


class InputThread implements Runnable
{
    private Thread t;
    private String threadName;
    private StringBuffer inputBuffer;

    InputThread(String _threadName, StringBuffer _buffer)
    {
        inputBuffer = _buffer;
        threadName = _threadName;
    }

    public void run()
    {
        for(;;)
        {
            Scanner userInput = new Scanner(System.in);

            // wait on input
            System.out.print("> ");
            while(!userInput.hasNext());

            if(userInput.hasNext())
            {
                //inputBuffer.append(userInput.nextLine());
                System.out.println(userInput.nextLine());
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