package dbgui;

import java.awt.Dimension;
import java.awt.ScrollPane;

import javax.swing.JTextArea;

import dbInterface.Output;

public class Console extends ScrollPane implements Output{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5035706321924916835L;

	public Console() {
		super();
		JTextArea ta = new JTextArea();
		this.add(ta);
		this.setPreferredSize(new Dimension(500, 50));
	}

	public void writeln(String str) {
		System.out.println("----"+str);
		
	}
}
