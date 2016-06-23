package importAssertion;

import importAssertion.Parser.Assertion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import dbInterface.Output;

public class InsertAssertion {
	Parser parser;
	Output out;
	private Connection connectToDB() throws SQLException, ClassNotFoundException{
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
		return conn;
	}
	
	
	public InsertAssertion(Output o,Parser p) throws ClassNotFoundException, SQLException {
		parser = p; // Datenbankverbindung
		out = o;
		Connection conn = connectToDB();
		out.writeln("Start DB Check...");
		checkTestSysRel(conn);
		checkAssertionSysRel(conn);
		
		for (Assertion as : p.precheckedAssertionsInsert) {
			if (checkName(conn, as)) {
				if (checkSelectTestSysRel(conn, as)) {
					insertAssertions(conn, as);
					Set<String> tables = getUsedTables(conn, as);
					boolean crFct = createDOFunction(conn, as, "DO"+as.name);
					out.writeln("Insert function: " + crFct);
					boolean crTri = createTrigger(conn, as, "DO"+as.name, tables); 
					out.writeln("Insert trigger: " + crTri);
					if(crFct && crTri){
						updateCorrectAssertions(conn, as);
					}
				}
			}
		}
		conn.close();
		out.writeln("Finished!");
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
			out.writeln("Error in assertion " + as.name + ":");
			out.writeln("\t" + e.getMessage());
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
			out.writeln("Insert " + as.name + "in DB.");
		} catch (Exception e) {
			out.writeln("Error in assertion " + as.name + ":");
			if (e.getMessage().contains("duplicate key value violates unique")) {
				out.writeln("\t ERROR: Assertion already exists");
			} else {
				out.writeln("\t" + e.getMessage());
			}

		} finally {
			ps.close();
		}
	}

	// insert Assertions
	private void updateCorrectAssertions(Connection conn, Assertion as)
			throws SQLException {
		Statement create = conn.createStatement();
		
		try {
			create.executeUpdate("UPDATE AssertionSysRel SET implementiert= "+true+ " WHERE "+ "Assertionname='"+as.name+"';");
			out.writeln("Update AssertionSysRel");
		} catch (Exception e) {
			out.writeln(e.getMessage());
		} finally {
			create.close();
		}
	}
	
	
	private boolean checkName(Connection conn, Assertion as) throws SQLException {
		Statement create = conn.createStatement();

		try {
			create.executeUpdate("CREATE TABLE " + as.name + " ( id INT )");
			create.executeUpdate("DROP TABLE " + as.name);
		} catch (Exception e) {
			out.writeln("Error in assertion " + as.name + ":");
			
			if(e.getMessage().contains("syntax error")){
				out.writeln("\tERROR: Invalid assertion name");
				try{
					Integer.parseInt(""+ as.name.toCharArray()[0]);
				out.writeln("\tAssertion should start with a letter");
			}catch(Exception ex){}}
			else{
				out.writeln(e.getMessage());
			}
			return false;

		} finally {
			create.close();
		}

		return true;

	}
	private Set<String> getUsedTables(Connection conn, Assertion as) throws SQLException {
		Set<String> output = new HashSet<>();
		Statement statement = conn.createStatement();
		ResultSet rs = statement.executeQuery("explain " + as.select);
		int cols = rs.getMetaData().getColumnCount();;
		while (rs.next()) {
			for (int i = 1; i <= cols; i++) {
				String res = rs.getString(i);
				if(res.toLowerCase().contains("on"))
				{
					String[] words = res.split(" ");
					for(int n = 0; n < words.length;n++)
						if(words[n].trim().toLowerCase().equals("on"))
						{
							output.add(words[n+1]);
						}
				}
			}
		}
		return output;
	}
	
	public boolean createDOFunction (Connection conn, Assertion as, String functionName) throws SQLException {
		Statement create = conn.createStatement();
		String cmd = null;
		try {
			String condition = as.condition.replace("'", "$$");
			cmd = "CREATE Function " + functionName + "() RETURNS TRIGGER AS " +
					"'Declare ErgebnisRec RECORD; BEGIN " +
					"SELECT INTO ErgebnisRec COUNT(*) AS Anzahl " +
					"FROM TestSysRel " +
					"WHERE NOT ( " + condition + "); " +
					"IF (ErgebnisRec.Anzahl >=1 )" +
					"THEN RAISE EXCEPTION" +
					"''ASSERTION " + as.name + " potenziell verletzt!'';" +
				    "END IF;" +
				    "RETURN NEW;" +
				    "END;'" +
				    "LANGUAGE 'plpgsql';";
			ResultSet result = create.executeQuery(cmd);
			out.writeln(result.toString());
		
		}catch(Exception ex){
			if(ex.getMessage().contains("Die Abfrage lieferte kein Ergebnis.")){
				return true;
			}
			else {
				out.writeln(ex.getMessage());
				out.writeln(cmd);
				return false;
			}
		}finally {
			create.close();
		}
		return true;
	}
	
	public boolean createTrigger(Connection conn, Assertion as, String functionName, Set<String> tables) throws SQLException  {
		Statement create = conn.createStatement();
		int result = -1;
		try {
			for (String table: tables){
				result = create.executeUpdate("CREATE TRIGGER CHECK"+as.name+table+" AFTER INSERT OR UPDATE OR DELETE ON "
						+ table + " FOR EACH ROW EXECUTE PROCEDURE " + functionName +"();");
		}
		}catch(Exception ex){
			out.writeln(ex.getMessage());
			return false;
		}finally {
			create.close();
		}
		if (result == 0){
			return true;
		}else{
			return false;
		}
			
	}
	
	
}
