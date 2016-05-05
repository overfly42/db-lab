package importAssertion;


public class ImportAssertionMain {

	public static void main(String[] args) {
		System.out.println("Arbeitsverzeichnis ist: " + System.getProperty("user.dir"));
		if(args.length == 0)
		{
			System.out.println("Bitte Datei angeben. Programm Ende");
			return;
		}
		
		new ImportAssertionMain(args[0]);
	}
	public ImportAssertionMain(String file)
	{
		System.out.println("Versuche Datei "+file+" einzulesen");
		Parser p = new Parser(file);
	}
}
