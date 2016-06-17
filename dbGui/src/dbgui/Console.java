package dbgui;

import java.awt.Dimension;
import java.awt.ScrollPane;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class Console extends ScrollPane {
	public Console() {
		super();
		JTextArea ta = new JTextArea();
		this.add(ta);
		this.setPreferredSize(new Dimension(500, 50));
	}
}
