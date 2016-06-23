package importAssertion;

import iface.DbInterface;
import importAssertion.Parser.Assertion;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.postgis.LineString;
import org.postgis.Point;
import org.postgis.Polygon;

public class CheckAssertion {

	Parser parser;
	DbInterface out;
	Connection conn;
	List<ConflictAssertion> conflictAssertions = new ArrayList<>();
	
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


	public CheckAssertion (DbInterface o,Parser p) throws ClassNotFoundException, SQLException {
		parser = p; // Datenbankverbindung
		out = o;
		this.conn = connectToDB();

		for (Assertion as : p.precheckedAssertionsCheck) {
			boolean name = checkName(conn, as);
			boolean testSysRel = checkSelect(conn, as);
			boolean duplicates = checkNoDuplicates(conn, as);
			if (name && testSysRel && duplicates){
				out.writeln("Assertion " + as.name + " wurde erfolgreich überprüfen.");
			}else{
				out.writeln("Assertion " + as.name + " war fehlerhaft.");
				break;
			}
			
			// Hier beginnt der eigentliche Check
			ConflictAssertion compromisedAsser = checkAssertionEntry(conn, as);
			conflictAssertions.add(compromisedAsser);
			//TODO: Funktioniert die Abfrage?
			if (compromisedAsser.assertionOk == false){
				out.writeln("Die Assertion " + as.name + " lieferte Fehler zurück.");
			}
			else{
				out.writeln("Die Assertion " + as.name + " lieferte keine Fehler zurück.");
			}
		}
		out.writeln("Check Assertions finished!");
		connClose();
	}
	
	/**
	 * Checkt die assertions und liefert ein Objekt mit der ID der fehlerhaften Tupel zurück
	 * sowie der Geometryobjekte
	 * @param conn
	 * @param as
	 * @return ConflictAssertion 
	 * @throws SQLException
	 */
	private ConflictAssertion checkAssertionEntry(Connection conn, Assertion as) throws SQLException{
		ConflictAssertion compromisedAsser = new ConflictAssertion(as);
		//Ist es ein Exists oder Not Exists?
		String[] s = as.condition.split(" ");
		Statement stmt = conn.createStatement();
		// Check for exists or not exists
		if (s[0].equals("not") && s[1].startsWith("exist")){
		// Not exist
		// Prüfen, ob Datensätze existieren mit Selcet
		// Wenn ja, Fehler
			try {
				ResultSet exists = stmt.executeQuery(as.select);
				if(exists.next() ==  false){
					// Keine Einträge existieren
					compromisedAsser.assertionOk = true;
				}else{
					while (exists.next()){
						//Einträge existieren, dieses sind Fehler und werden zurück gegeben
						Map<String, Object> entry = new HashMap<>();
						int id = exists.getInt("id");
						entry.put("id", id);
						// Geometry-Objecte besorgen
						try{
							Point pos = (Point) exists.getObject("pos"); //pos geometry Point
							entry.put("pos", pos);
							
						}catch(Exception e) {
							//Nichts tun
						}
						try{
							Polygon umriss = (Polygon) exists.getObject("umriss"); //umriss geometry Polygon
							entry.put("umriss", umriss);
							}catch(Exception e) {
								//Nichts tun
							}
						try{
							LineString path = (LineString) exists.getObject("path"); //path geometry Linestring
							entry.put("path", path);
							}catch(Exception e) {
								//Nichts tun
							}

						compromisedAsser.addListEntry(entry);
					}
				}
			} catch (Exception e) {
				out.writeln("Error in assertion " + as.name + ":");
				out.writeln("\t" + e.getMessage());
			} finally {
				stmt.close();
			}	
		}
		else if (s[0].startsWith("exist")){
		// Select ausführen
			ResultSet exists = stmt.executeQuery(as.select);
			if(exists.next() ==  false){
			// Keine Einträge ->Fehler	
			}else{
				compromisedAsser.assertionOk = true;
			}
		}
		return compromisedAsser;
	}
	
	public void connClose() throws SQLException{
		conn.close();
	}

	// check Select
	private boolean checkSelect(Connection conn, Assertion as) throws SQLException {
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
	private boolean checkNoDuplicates(Connection conn, Assertion as)
			throws SQLException {
		PreparedStatement ps = conn.prepareStatement("Select * from AssertionSysRel VALUES (?)");
		ps.setObject(1, as.name);
		try {
			ResultSet rs = ps.executeQuery();
			if (rs.next() == true){
				return false;
			}
			
		} catch (Exception e) {
			out.writeln("Error in assertion " + as.name + ":");
			out.writeln("\t" + e.getMessage());
			return false;

		} finally {
			ps.close();
		}
		return true;
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


}

