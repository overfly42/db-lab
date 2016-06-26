package importAssertion;

import iface.DbInterface;

import java.sql.SQLException;

public class ImportAssertionMain {

	OutputConsole o;

	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		System.out.println("Arbeitsverzeichnis ist: " + System.getProperty("user.dir"));
		if (args.length == 0) {
			System.out.println("Bitte Datei angeben. Programm Ende");
			return;
		}

		new ImportAssertionMain(args[0]);
	}

	public ImportAssertionMain(String file) throws ClassNotFoundException, SQLException {
		this(file, new OutputConsole());

	}

	public ImportAssertionMain(String file, DbInterface o) throws ClassNotFoundException, SQLException
	{
		o.writeln("Versuche Datei " + file + " einzulesen");
		Parser p = new Parser(o,file);
		InsertAssertion i = new InsertAssertion(o,p);
		o.writeln("---------------------------------");
		DropAssertion d = new DropAssertion( p , o);
		o.writeln("---------------------------------");
		CheckAssertion c = new CheckAssertion(o, p);
	}
}
