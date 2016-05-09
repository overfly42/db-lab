package importAssertion;

import importAssertion.Parser.Assertion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CheckDB {
	Parser parser;

	public CheckDB(Parser p) throws ClassNotFoundException, SQLException {
		parser = p; // Datenbankverbindung
		Class.forName("org.postgresql.Driver");
		String url = "jdbc:postgresql://localhost:5432/geo";
		Connection conn;
		try{
			conn = DriverManager.getConnection(url, "postgres", "admin");
		}catch (Exception e){
			try{
					conn = DriverManager.getConnection(url, "postgres", "");
			}
			catch (Exception ex){
				 	conn = DriverManager.getConnection(url, "admin", "admin");	
			}
			
		}
		checkTestSysRel(conn);
		checkAssertionSysRel(conn);
		for(Assertion as : p.precheckedAssertions){
			if(checkName(conn, as)){
				if (checkSelectTestSysRel(conn, as)){
					insertAssertions(conn, as);
				}
			}
				
		}
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
	
	//check Select in TestSysRel
	private boolean checkSelectTestSysRel(Connection conn, Assertion as) throws SQLException {
		Statement stmt = conn.createStatement();
		try {
			ResultSet exists = stmt.executeQuery(as.condition);
		}
		catch (Exception e){
			System.out.println("Error in assertion " + as.name + ":");
			System.out.println(e.getMessage());
			return false;
		}
		finally {
			stmt.close();
		}
		return true;
	}
	
	//insert Assertions
	private void insertAssertions (Connection conn, Assertion as) throws SQLException {
		PreparedStatement ps = conn
				.prepareStatement("INSERT INTO AssertionSysRel VALUES (?,?)");
		ps.setObject(1, as.name);
		ps.setObject(2, as.condition);
		try {
			ps.execute();	
		}
		catch (Exception e){
			System.out.println("Error in assertion " + as.name + ":");
			if(e.getMessage().contains("duplicate key value violates unique")){
				System.out.println("Assertion already exists");
			}
			else{
				System.out.println(e.getMessage());
			}
			
		}
		finally {
			ps.close();
		}
	}
	
	private boolean checkName (Connection conn, Assertion as) throws SQLException {
		Statement create = conn.createStatement();

		try {
			create.executeUpdate("CREATE TABLE " + as.name + " ( id INT )");
			create.executeUpdate("DROP TABLE " + as.name);
		}
		catch (Exception e){
			System.out.println("Error in assertion " + as.name + ":");
			System.out.println("Invalid assertion name");	
			return false;
			
		} finally {
			create.close();
		}
		
		return true;
		
	}

}
