package importAssertion;

import iface.DbInterface;

import java.sql.SQLException;

public class ImportAssertionMain {

	OutputConsole o;
	Parser p;
	InsertAssertion i;
	DropAssertion d;
	CheckAssertion c;

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

	public ImportAssertionMain(String file, DbInterface o) throws ClassNotFoundException, SQLException {
		o.writeln("Versuche Datei " + file + " einzulesen");
		p = new Parser(o, file);
		i = new InsertAssertion(o, p);
		o.writeln("---------------------------------");
		d = new DropAssertion(p, o);
		o.writeln("---------------------------------");
		c = new CheckAssertion(o, p);
	}

	public OutputConsole getO() {
		return o;
	}

	public void setO(OutputConsole o) {
		this.o = o;
	}

	public InsertAssertion getI() {
		return i;
	}


	public CheckAssertion getC() {
		return c;
	}
	public DropAssertion getD() {
		return d;
	}

}
