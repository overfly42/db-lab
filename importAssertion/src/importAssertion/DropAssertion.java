package importAssertion;

import iface.DbInterface;
import importAssertion.Parser.Assertion;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

public class DropAssertion {
	Parser parser;
	DbInterface out;

	public DropAssertion(Parser p, DbInterface o) throws ClassNotFoundException, SQLException {
		parser = p; // Datenbankverbindung
		out = o;
		Connection conn = connectToDB();

		for (Assertion as : p.precheckedAssertionsDrop) {
			// Delete Trigger	
			Set<String> tables = getUsedTables(conn, as);
			boolean delTri = deleteTrigger(conn, as, "DO"+as.name, tables); 
			// Delete Function
			boolean delFct = deleteDOFunction(conn, as, "DO"+as.name );
			//AssertionSysRel
			boolean delAsSysRel = deleteFromAssertionSysRel(conn, as);
			if (delAsSysRel && delFct && delTri){
				out.writeln("Assertion " + as.name + " wurde erfolgreich gelöscht.");
			}else{
				out.writeln("Error: Assertion " + as.name + " konnte nicht gelöscht werden.");
			}
		}
		conn.close();
		out.writeln("Finished with Drop!");
	}
	
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
	// delete Assertions from AssertionSysRel
	private boolean deleteFromAssertionSysRel(Connection conn, Assertion as)
			throws SQLException {
		Statement create = conn.createStatement();
		String cmd = "DELETE FROM AssertionSysRel WHERE Assertionname= '" + as.name + "';"; 
		try {
			create.execute(cmd);
			out.writeln("Deleted " + as.name + "in AssertionSysRel.");
		} catch (Exception e) {
			out.writeln("Fehler beim Löschen aus AssertionSysRel");
			out.writeln("Error in assertion " + as.name + ":");
			out.writeln("\t" + e.getMessage());
			return false;
		} finally {
			create.close();
		}
		return true;
	}

	// Delete Function
	public boolean deleteDOFunction (Connection conn, Assertion as, String functionName) throws SQLException {
		Statement create = conn.createStatement();
		String cmd = null;
		try {
			cmd = "DROP FUNCTION IF EXISTS " + functionName	 + "()  CASCADE;"; 
			create.execute(cmd);

		}catch(Exception ex){
			out.writeln("Fehler beim Löschen der Function");
			out.writeln(ex.getMessage());
			out.writeln(cmd);
			return false;
		}finally {
			create.close();
		}
		return true;
	}

	// Delete Trigger
	public boolean deleteTrigger(Connection conn, Assertion as, String functionName, Set<String> tables) throws SQLException  {
		Statement create = conn.createStatement();
		try {
			for (String table: tables){
				create.executeUpdate("DROP TRIGGER IF EXISTS "+as.name+table+" ON "
								+ table + " CASCADE;");
			}
		}catch(Exception ex){
			out.writeln("Fehler beim Löschen der Trigger");
			out.writeln(ex.getMessage());
			return false;
		}finally {
			create.close();
		}
		return true;
	}
	private Set<String> getUsedTables(Connection conn, Assertion as) throws SQLException {
		Set<String> output = new HashSet<>();
		Statement statement = conn.createStatement();
		ResultSet result = statement.executeQuery("Select tgname from PG_trigger where tgname like '"+as.name+"';");
		while (result.next()){ 
			String tgname = result.getString("tgname");
			String table = tgname.replaceFirst(as.name, "");
			output.add(table);
		}
		return output;
	}

}




