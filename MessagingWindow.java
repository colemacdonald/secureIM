
import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.*;  
import java.lang.StringBuffer;
import java.util.Scanner;
import java.io.*;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.DateFormat;

public class MessagingWindow implements ActionListener {
	
	private JFrame frame;
	private JTextField inputTextField;
	private JScrollPane scrollPane;
	private JTextArea messageDisplayArea;
	private StringBuffer userInputBuffer;
	//Object inputReady;

	MessagingWindow(StringBuffer userInputBuffer) { //Object inputReady){
		this.userInputBuffer = userInputBuffer;
		//this.inputReady = inputReady;

		this.frame = new JFrame("IM");
        createTextField();
        createMessageWindow();
	}

	public StringBuffer getUserInputBuffer() {
		return userInputBuffer;
	}

	public synchronized void writeToMessageWindow(String message) {
		messageDisplayArea.append(message + "\n");
	}

	private void createTextField() {
        inputTextField = new JTextField(20);
		inputTextField.setBounds(100,250,200,30);
		
		messageDisplayArea = new JTextArea();
		messageDisplayArea.setLineWrap(true);
		messageDisplayArea.setWrapStyleWord(true);
		messageDisplayArea.setEditable(false);
		

		scrollPane = new JScrollPane(messageDisplayArea);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		Dimension d = new Dimension(300, 300);
		scrollPane.setPreferredSize(d);
		scrollPane.setBounds(30, 30, 340, 200);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        frame.add(inputTextField);
        frame.setSize(400, 400);
        frame.setLayout(null);  
        frame.setVisible(true);
        inputTextField.addKeyListener(new KeyAdapter() {

		    public void keyReleased(KeyEvent event) {	

		 		if(event.getKeyCode()==KeyEvent.VK_ENTER){
		 			
					//synchronized(inputReady) {
						String content = inputTextField.getText();
						
						DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
						Date date = new Date();
						writeToMessageWindow(dateFormat.format(date) + " > " + content);
			        	//System.out.println(content);
			        	userInputBuffer.append(content);
			        	inputTextField.setText("");
			        	//inputReady.notify();
		        	//}
		       	}
		    }
		});
	}	

	public void close() {
		this.frame.dispose();
	}

	void createMessageWindow() {
		//do nothing for now
	}

	public void actionPerformed(ActionEvent e) {
		System.out.println("action performed!");
	}
}