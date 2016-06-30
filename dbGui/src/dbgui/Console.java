package dbgui;

import java.awt.Dimension;
import java.awt.ScrollPane;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import iface.DbInterface;

public class Console extends JScrollPane implements DbInterface {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5035706321924916835L;

	JTextArea ta;

	public Console() {
		super();
		this.getVerticalScrollBar().setUnitIncrement(20);
		ta = new JTextArea();
		this.setViewportView(ta);
		this.setPreferredSize(new Dimension(500, 150));
	}

	public void writeln(String str) {
		ta.append(str + "\n");

	}

	void clear() {
		ta.setText("");
	}
}
