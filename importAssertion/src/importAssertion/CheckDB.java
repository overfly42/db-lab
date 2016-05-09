package importAssertion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CheckDB {
	Parser parser;

	public CheckDB(Parser p) throws ClassNotFoundException, SQLException {
		parser = p; // Datenbankverbindung
		Class.forName("org.postgresql.Driver");
		String url = "jdbc:postgresql://localhost:5432/geo";
//		Connection conn = DriverManager.getConnection(url, "postgres", "admin");
		 Connection conn = DriverManager.getConnection(url, "postgres", "");
		// Connection conn = DriverManager.getConnection(url, "admin", "admin");
		checkTestSysRel(conn);
		checkAssertionSysRel(conn);
		conn.close();
	}

	// TestSysRel
	private void checkTestSysRel(Connection conn) throws SQLException {
		Statement create = conn.createStatement();

		try {
			ResultSet exists = create.executeQuery(
					"SELECT count(relname) AS hasTable FROM pg_class WHERE lower(relname) = lower('TestSysRel')");

			if (exists.next() && exists.getInt("hasTable") == 0) {
				create.executeUpdate("CREATE TABLE TestSysRel (Attribut INTEGER NOT NULL, PRIMARY KEY (Attribut))");
				create.executeUpdate("INSERT INTO TestSysRel VALUES(1)");
			}
		} finally {
			create.close();
		}
	}

	// AssertionSysRel
	private void checkAssertionSysRel(Connection conn) throws SQLException {
		Statement create = conn.createStatement();

		try {
			ResultSet exists = create.executeQuery(
					"SELECT count(relname) AS hasTable FROM pg_class WHERE lower(relname) = lower('AssertionSysRel')");

			if (exists.next() && exists.getInt("hasTable") == 0) {
				create.executeUpdate("CREATE TABLE AssertionSysRel (" + "Assertionname VARCHAR(40) NOT NULL,"
						+ "Bedingung VARCHAR(800) NOT NULL," + "implementiert BOOL DEFAULT FALSE,"
						+ "PRIMARY KEY (Assertionname)" + ")");
			}
		} finally {
			create.close();
		}
	}

}
