
import javax.swing.*;
import java.awt.event.*;  
import java.lang.StringBuffer;
import java.util.Scanner;
import java.io.*;

public class UserInput implements ActionListener{
	
	JFrame frame;
	JTextField inputTextField;
	StringBuffer userInput;
	Object inputReady;

	UserInput(StringBuffer _userInput, Object _inputReady){
		this.userInput = _userInput;
		this.inputReady = _inputReady;
	}

	void CreateTextField(){
		frame = new JFrame("IM");
        inputTextField = new JTextField(20);
        inputTextField.setBounds(50,100, 200,30);
        frame.add(inputTextField);
        frame.setSize(400, 400);
        frame.setLayout(null);  
        frame.setVisible(true);
        inputTextField.addKeyListener(new KeyAdapter() {

		    public void keyReleased(KeyEvent event) {	

		 		if(event.getKeyCode()==KeyEvent.VK_ENTER){
		 			
					synchronized(inputReady) {
			        	String content = inputTextField.getText();
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