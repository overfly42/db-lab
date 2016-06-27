package dbgui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class SidePanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8649322078099600904L;
	GuiContainer gui;

	public SidePanel(GuiContainer g) {
		gui = g;
		JButton btn = new JButton();
		btn.setText("Drück mich");
		final SidePanel sp = this;
		btn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				//JOptionPane.showConfirmDialog(sp, "Danke fürs Drücken");
				gui.pa.repaint();
			}
		});
		this.add(btn);
	}
}
