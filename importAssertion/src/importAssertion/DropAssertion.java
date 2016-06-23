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

public class DropAssertion {
	Parser parser;

	public DropAssertion(Parser p) throws ClassNotFoundException, SQLException {
		parser = p; // Datenbankverbindung
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
				System.out.println("Assertion " + as.name + " wurde erfolgreich gelöscht.");
			}else{
				System.out.println("Error: Assertion " + as.name + " konnte nicht gelöscht werden.");
			}
		}
		conn.close();
		System.out.println("Finished with Drop!");
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
		PreparedStatement ps = conn.prepareStatement("DELETE FROM AssertionSysRel VALUES (?)");
		ps.setObject(1, as.name);
		try {
			ps.execute();
			System.out.println("Deleted " + as.name + "in AssertionSysRel.");
		} catch (Exception e) {
			System.out.println("Error in assertion " + as.name + ":");
			System.out.println("\t" + e.getMessage());
			return false;
		} finally {
			ps.close();
		}
		return true;
	}

	// Delete Function
	public boolean deleteDOFunction (Connection conn, Assertion as, String functionName) throws SQLException {
		Statement create = conn.createStatement();
		String cmd = null;
		try {
			cmd = "DROP FUNCTION IF EXISTS " + functionName + "()  CASCADE;"; 
			ResultSet result = create.executeQuery(cmd);
			System.out.println(result);

		}catch(Exception ex){
			System.out.println(ex.getMessage());
			System.out.println(cmd);
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
			System.out.println(ex.getMessage());
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




