package importAssertion;

import java.sql.SQLException;

public class ImportAssertionMain {


	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		System.out.println("Arbeitsverzeichnis ist: " + System.getProperty("user.dir"));
		if (args.length == 0) {
			System.out.println("Bitte Datei angeben. Programm Ende");
			return;
		}

		new ImportAssertionMain(args[0]);
	}

	public ImportAssertionMain(String file) throws ClassNotFoundException, SQLException {
		System.out.println("Versuche Datei " + file + " einzulesen");
		Parser p = new Parser(file);
		CheckDB c = new CheckDB(p);

	}
}