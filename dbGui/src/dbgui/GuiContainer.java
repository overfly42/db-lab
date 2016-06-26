package dbgui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.SQLException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

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

	JFileChooser assertionFile;
	JFileChooser insertSQLFile;
	
	Component frame;
	
	public GuiContainer() {
		initMenu();
		initComponents();
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
		
		insertSQLFile = new JFileChooser(".");
		assertionFile = new JFileChooser(".");
		
		frame = this;
	}

	private void initMenu() {
		JMenuBar jmb = new JMenuBar();
		JMenu f = new JMenu("File");
		jmb.add(f);
		JMenuItem jmi;
		jmi = new JMenuItem("Open Assertions");
		jmi.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				int option = assertionFile.showOpenDialog(frame);
				if(option == JFileChooser.CANCEL_OPTION || !assertionFile.getSelectedFile().exists())
					return;
				try {
					importAssertions = new ImportAssertionMain(assertionFile.getSelectedFile().getAbsolutePath(),con);
					JOptionPane.showMessageDialog(frame, "Insert done!");
				} catch (ClassNotFoundException |  SQLException e) {
					e.printStackTrace();
				}
							
			}
		});
		f.add(jmi);
		jmi = new JMenuItem("Insert Database");
		jmi.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int option = insertSQLFile.showOpenDialog(frame);
				if(option == JFileChooser.CANCEL_OPTION || !insertSQLFile.getSelectedFile().exists())
					return;
				try {
					importSQL = new Main(insertSQLFile.getSelectedFile());
					JOptionPane.showMessageDialog(frame, "Insert done!");
				} catch (ClassNotFoundException | ParserConfigurationException | SAXException | IOException e) {
					e.printStackTrace();
				}
				
			}
		});
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
