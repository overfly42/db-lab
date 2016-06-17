package dbgui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import importAssertion.ImportAssertionMain;
import importSQL.Main;

public class GuiContainer extends JFrame {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new GuiContainer();
	}

	SidePanel sp;
	PaintingArea pa;
	Console con;

	Main importSQL;
	ImportAssertionMain importAssertions;

	public GuiContainer() {
		initComponents();
		initMenu();
	}

	private void initComponents() {
		this.setLayout(new BorderLayout());
		this.setSize(new Dimension(700, 500));
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setTitle("Dies ist ein Titel");
		sp = new SidePanel();
		pa = new PaintingArea();
		con = new Console();
		this.add(sp, BorderLayout.EAST);
		this.add(pa, BorderLayout.CENTER);
		this.add(con, BorderLayout.SOUTH);
		this.setVisible(true);
	}

	private void initMenu() {
		JMenuBar jmb = new JMenuBar();
		JMenu f = new JMenu("File");
		jmb.add(f);
		JMenuItem jmi;
		jmi = new JMenuItem("Open Assertions");
		f.add(jmi);
		jmi = new JMenuItem("Insert Database");
		f.add(jmi);
		jmi = new JMenuItem("Exit");
		jmi.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);

			}
		});
		f.addSeparator();
		f.add(jmi);

		this.setJMenuBar(jmb);
	}

}
