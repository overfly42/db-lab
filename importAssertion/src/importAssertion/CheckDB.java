package importAssertion;

import importAssertion.Parser.Assertion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CheckDB {
	Parser parser;

	public CheckDB(Parser p) throws ClassNotFoundException, SQLException {
		parser = p; // Datenbankverbindung
		Class.forName("org.postgresql.Driver");
		String url = "jdbc:postgresql://localhost:5432/geo";
		Connection conn;
		try {
			conn = DriverManager.getConnection(url, "postgres", "admin");
		} catch (Exception e) {
			try {
				conn = DriverManager.getConnection(url, "postgres", "");
			} catch (Exception ex) {
				conn = DriverManager.getConnection(url, "admin", "admin");
			}

		}
		System.out.println("Start DB Check...");
		checkTestSysRel(conn);
		checkAssertionSysRel(conn);
		List<String> tables = new ArrayList<>();
		for (Assertion as : p.precheckedAssertions) {
			if (checkName(conn, as)) {
				if (checkSelectTestSysRel(conn, as)) {
					insertAssertions(conn, as);
					tables.addAll(getUsedTables(conn, as));
					boolean crFct = createDOFunction(conn, as, "DO"+as.name);
					System.out.println("Insert function: " + crFct);
				}
			}

		}
		System.out.println("Used " + tables.size() + " tables");
		conn.close();
		System.out.println("Finished!");
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

	// check Select in TestSysRel
	private boolean checkSelectTestSysRel(Connection conn, Assertion as) throws SQLException {
		Statement stmt = conn.createStatement();
		try {
			ResultSet exists = stmt.executeQuery(as.select);
		} catch (Exception e) {
			System.out.println("Error in assertion " + as.name + ":");
			System.out.println("\t" + e.getMessage());
			return false;
		} finally {
			stmt.close();
		}
		return true;
	}

	// insert Assertions
	private void insertAssertions(Connection conn, Assertion as)
			throws SQLException {
		PreparedStatement ps = conn.prepareStatement("INSERT INTO AssertionSysRel VALUES (?,?)");
		ps.setObject(1, as.name);
		ps.setObject(2, as.condition);
		try {
			ps.execute();
			System.out.println("Insert " + as.name + "in DB.");
		} catch (Exception e) {
			System.out.println("Error in assertion " + as.name + ":");
			if (e.getMessage().contains("duplicate key value violates unique")) {
				System.out.println("\t ERROR: Assertion already exists");
			} else {
				System.out.println("\t" + e.getMessage());
			}

		} finally {
			ps.close();
		}
	}

	private boolean checkName(Connection conn, Assertion as) throws SQLException {
		Statement create = conn.createStatement();

		try {
			create.executeUpdate("CREATE TABLE " + as.name + " ( id INT )");
			create.executeUpdate("DROP TABLE " + as.name);
		} catch (Exception e) {
			System.out.println("Error in assertion " + as.name + ":");
			
			if(e.getMessage().contains("syntax error")){
				System.out.println("\tERROR: Invalid assertion name");
				try{
					Integer.parseInt(""+ as.name.toCharArray()[0]);
				System.out.println("\tAssertion should start with a letter");
			}catch(Exception ex){}}
			else{
				System.out.println(e.getMessage());
			}
			return false;

		} finally {
			create.close();
		}

		return true;

	}
	private List<String> getUsedTables(Connection conn, Assertion as) throws SQLException {
		List<String> output = new ArrayList<>();
		Statement statement = conn.createStatement();
		ResultSet rs = statement.executeQuery("explain " + as.select);
		int cols = rs.getMetaData().getColumnCount();
		System.out.println("-------------");
		System.out.println("Num of cols: " + cols);
		while (rs.next()) {
			for (int i = 1; i <= cols; i++) {
				String res = rs.getString(i);
				if(res.toLowerCase().contains("on"))
				{
//					System.out.println(res.trim());
					String[] words = res.split(" ");
					for(int n = 0; n < words.length;n++)
						if(words[n].trim().toLowerCase().equals("on"))
						{
	//						System.out.println(words[n+1]);
							output.add(words[n+1]);
						}
				}
			}
		}
		System.out.println("-------------");
		return output;
	}
	
	public boolean createDOFunction (Connection conn, Assertion as, String functionName) throws SQLException {
		Statement create = conn.createStatement();
		try {
			ResultSet result = create.executeQuery("CREATE Function " + functionName + "() RETURNS TRIGGER AS " +
							"'Declare ErgebnisRec RECORD; BEGIN " +
							"SELECT INTO ErgebnisRec COUNT(*) AS Anzahl " +
							"FROM TestSysRel " +
							"WHERE NOT ( " + as.condition + "); " +
							"IF (ErgebnisRec.Anzahl >=1 )" +
							"THEN RAISE EXCEPTION" +
							"''ASSERTION " + as.name + " potenziell verletzt!'';" +
						    "END IF;" +
						    "RETURN NEW;" +
						    "END;'" +
						    "LANGUAGE 'plpgsql';" );
			System.out.println(result);
		
		}catch(Exception ex){
			System.out.println("CREATE Function " + functionName + "() RETURNS TRIGGER AS " +
					"'Declare ErgebnisRec RECORD; BEGIN " +
					"SELECT INTO ErgebnisRec COUNT(*) AS Anzahl " +
					"FROM TestSysRel " +
					"WHERE NOT ( " + as.condition + "); " +
					"IF (ErgebnisRec.Anzahl >=1 )" +
					"THEN RAISE EXCEPTION" +
					"''ASSERTION " + as.name + " potenziell verletzt!'';" +
				    "END IF;" +
				    "RETURN NEW;" +
				    "END;'" +
				    "LANGUAGE 'plpgsql';" );
			System.out.println(ex);
			return false;
		}finally {
			create.close();
		}
		return true;
		
	}
	
}
