package importAssertion;

import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ImportAssertionMain {
	
	Connection conn;

	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		System.out.println("Arbeitsverzeichnis ist: " + System.getProperty("user.dir"));
		if(args.length == 0)
		{
			System.out.println("Bitte Datei angeben. Programm Ende");
			return;
		}
		
		new ImportAssertionMain(args[0]);
	}
	
	public ImportAssertionMain(String file) throws ClassNotFoundException, SQLException {
		
		System.out.println("Versuche Datei "+file+" einzulesen");
		Parser p = new Parser(file);
		
		// Datenbankverbindung
		Class.forName("org.postgresql.Driver");
		String url = "jdbc:postgresql://localhost:5432/geo";
		Connection conn = DriverManager.getConnection(url, "postgres", "admin");
		//Connection conn = DriverManager.getConnection(url, "postgres", "");
		//Connection conn = DriverManager.getConnection(url, "admin", "admin");
			
		

		
		
		// Verbindung schlieﬂen
		conn.close();
		
	}
		
		// TestSysRel
		private void TestTable() throws SQLException {
	        Statement create = conn.createStatement();

	        try {
	            ResultSet exists = create.executeQuery("SELECT count(relname) AS hasTable FROM pg_class WHERE lower(relname) = lower('TestSysRel')");

	            if(exists.next() && exists.getInt("hasTable") == 0){
	                create.executeUpdate("CREATE TABLE TestSysRel (Attribut INTEGER NOT NULL, PRIMARY KEY (Attribut))");
	                create.executeUpdate("INSERT INTO TestSysRel VALUES(1)");
	            }
	        }
	        finally{
	            create.close();
	        }
	    }

		// AssertionSysRel
		private void checkTable() throws SQLException {
	        Statement create = conn.createStatement();

	        try {
	            ResultSet exists = create.executeQuery("SELECT count(relname) AS hasTable FROM pg_class WHERE lower(relname) = lower('AssertionSysRel')");

	            if(exists.next() && exists.getInt("hasTable") == 0){
	                create.executeUpdate(
	                    "CREATE TABLE AssertionSysRel (" +
	                        "Assertionname VARCHAR(40) NOT NULL," +
	                        "Bedingung VARCHAR(800) NOT NULL," +
	                        "implementiert BOOL DEFAULT FALSE," +
	                        "PRIMARY KEY (Assertionname)" +
	                    ")"
	                );
	            }
	        }
	        finally{
	            create.close();
	        }
	    }

	}
			
