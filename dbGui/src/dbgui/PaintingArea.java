package dbgui;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JPanel;

public class PaintingArea extends JPanel {
	public PaintingArea() {
		this.setPreferredSize(new Dimension(500, 500));
		this.setOpaque(true);
		this.setBackground(Color.CYAN);
	}
}
