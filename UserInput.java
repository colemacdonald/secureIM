
import javax.swing.*;
import java.awt.event.*;  
import java.lang.StringBuffer;
import java.util.Scanner;
import java.io.*;

public class UserInput implements ActionListener{
	
	JFrame frame;
	JTextField inputTextField;
	StringBuffer userInput;

	UserInput(StringBuffer _userInput){
		this.userInput = _userInput;
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
		        	String content = inputTextField.getText();
		        	//System.out.println(content);
		        	userInput.append(content);
		        	inputTextField.setText("");
		       	}
		    }
		   });	
	}

	public void actionPerformed(ActionEvent e) {
		// Do thing for now
	}
}