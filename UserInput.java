
import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.*;  
import java.lang.StringBuffer;
import java.util.Scanner;
import java.io.*;

public class UserInput implements ActionListener{
	
	JFrame frame;
	JTextField inputTextField;
	JScrollPane scrollPane;
	StringBuffer userInput;
	JTextArea messageDisplayArea;
	Object inputReady;

	UserInput(StringBuffer _userInput, Object _inputReady){
		this.userInput = _userInput;
		this.inputReady = _inputReady;
	}

	void CreateTextField(){
		frame = new JFrame("IM");
        inputTextField = new JTextField(20);
		inputTextField.setBounds(100,350,200,30);
		
		messageDisplayArea = new JTextArea();
		messageDisplayArea.setLineWrap(true);
		messageDisplayArea.setWrapStyleWord(true);
		

		scrollPane = new JScrollPane(messageDisplayArea);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		Dimension d = new Dimension(300, 300);
		scrollPane.setPreferredSize(d);
		scrollPane.setBounds(30, 30, 340, 200);
		frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        frame.add(inputTextField);
        frame.setSize(400, 400);
        frame.setLayout(null);  
        frame.setVisible(true);
        inputTextField.addKeyListener(new KeyAdapter() {

		    public void keyReleased(KeyEvent event) {	

		 		if(event.getKeyCode()==KeyEvent.VK_ENTER){
		 			
					synchronized(inputReady) {
						String content = inputTextField.getText();
						messageDisplayArea.append(content);
			        	//System.out.println(content);
			        	userInput.append(content);
			        	inputTextField.setText("");
			        	inputReady.notify();
		        	}
		       	}
		    }
		});
	}

	public void actionPerformed(ActionEvent e) {
		System.out.println("action performed!");
	}
}