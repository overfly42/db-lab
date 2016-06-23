package importAssertion;

import java.sql.SQLException;

public class ImportAssertionMain {

	Output o;

	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		System.out.println("Arbeitsverzeichnis ist: " + System.getProperty("user.dir"));
		if (args.length == 0) {
			System.out.println("Bitte Datei angeben. Programm Ende");
			return;
		}

		new ImportAssertionMain(args[0]);
	}

	public ImportAssertionMain(String file) throws ClassNotFoundException, SQLException {
		this(file, new Output());

	}

	public ImportAssertionMain(String file,Output o) throws ClassNotFoundException, SQLException
	{
		o.writeln("Versuche Datei " + file + " einzulesen");
		Parser p = new Parser(o,file);
		InsertAssertion c = new InsertAssertion(o,p);
	}
}
