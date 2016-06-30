package dbgui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

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
		List<Component> comps = new ArrayList<>();
		gui = g;
		JButton btn;
		btn = new JButton();
		btn.setText("next");
		btn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				gui.pa.next();

			}
		});
		comps.add(btn);

		comps.add(gui.pa.lbl);

		btn = new JButton();
		btn.setText("back");
		final SidePanel sp = this;
		btn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// JOptionPane.showConfirmDialog(sp, "Danke fürs Drücken");
				gui.pa.back();
			}
		});
		comps.add(btn);

		this.setLayout(new GridLayout(comps.size(), 1));
		this.setPreferredSize(new Dimension(200, 500));
		for (Component c : comps)
			this.add(c);
	}
}
