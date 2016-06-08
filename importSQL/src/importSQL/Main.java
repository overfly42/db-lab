package importSQL;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.postgis.LineString;
import org.postgis.LinearRing;
import org.postgis.Point;
import org.postgis.Polygon;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Main {

	Document doc;
	NodeList nodes;
	NodeList ways;
	NodeList relations;
	Map<String, Integer> tags;
	Map<Long, MyNode> nodesMap;
	Map<Long, MyWay> waysMap;

	private class MyNode {
		public Point p;
		public List<String> key;
		public List<String> value;

		@SuppressWarnings("unused")
		public MyNode() {
			key = new ArrayList<>();
			value = new ArrayList<>();
		}

		public MyNode(Point point, List<String> keys, List<String> values) {
			this.p = point;
			this.key = keys;
			this.value = values;

		}

		public Point getP() {
			return p;
		}

	}

	private class MyWay {
		public LineString path;
		public List<String> key;
		public List<String> value;
		public boolean closed = false;

		public MyWay() {
			key = new ArrayList<>();
			value = new ArrayList<>();
		}

		public MyWay(LineString path, List<String> keys, List<String> values, boolean closed) {
			this.path = path;
			this.key = keys;
			this.value = values;
			this.closed = closed;
		}

		public boolean isClosed() {
			return closed;
		}
	}

	public static void main(String[] args) throws ClassNotFoundException {

		try {
			System.out.println("Hello World");
			new Main();

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public Main() throws ParserConfigurationException, SAXException, IOException, ClassNotFoundException {
		System.out.println("Working dir is: " + System.getProperty("user.dir"));
		if (!createDoc())
			return;

		createRootElements();

		connectToPostgres();

		// fetchTags();
		// testoutput();
	}

	public boolean createDoc() throws ParserConfigurationException, SAXException, IOException {
		File input = new File("map.xml");
		if (!input.exists()) {
			System.out.println("File map.xml does not exist");
			return false;
		}
		DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
		DocumentBuilder dbBuild = dbFac.newDocumentBuilder();
		doc = dbBuild.parse(input);
		return true;
	}

	public void createRootElements() {
		doc.getDocumentElement().normalize();
		nodes = doc.getElementsByTagName("node");
		createNodes(nodes);

		ways = doc.getElementsByTagName("way");
		createWays(ways);

		relations = doc.getElementsByTagName("relation");
		System.out.println("Roots created");
	}

	private void createNodes(NodeList nodes) {
		nodesMap = new HashMap<>();
		for (int i = 0; i < nodes.getLength(); i++) {
			Long id = Long.valueOf(nodes.item(i).getAttributes().getNamedItem("id").getNodeValue());
			String sLat = nodes.item(i).getAttributes().getNamedItem("lat").getNodeValue();
			String sLon = nodes.item(i).getAttributes().getNamedItem("lat").getNodeValue();
			double lat = Double.valueOf(sLat);
			double lon = Double.valueOf(sLon);
			Point point = new Point();
			point.setX(lat);
			point.setY(lon);

			List<String> keys = new ArrayList<>();

			List<String> values = new ArrayList<>();

			// Tags fetchen
			NodeList n = nodes.item(i).getChildNodes();
			// System.out.println("Node " + i +": id " + id + " point (x,y) (" +
			// point.x +", " + point.y + ")");
			for (int j = 0; j < n.getLength(); j++) {
				if (n.item(j).getNodeName().equals("tag")) {
					String k = n.item(j).getAttributes().getNamedItem("k").getNodeValue();
					String v = n.item(j).getAttributes().getNamedItem("v").getNodeValue();
					// System.out.println("Tags: " + k + " " + v);
					keys.add(k);
					values.add(v);
				}
			}
			MyNode myNode = new MyNode(point, keys, values);
			nodesMap.put(id, myNode);
		}
	}

	private void createWays(NodeList ways) {
		waysMap = new HashMap<>();
		for (int i = 0; i < ways.getLength(); i++) {
			Long id = Long.valueOf(ways.item(i).getAttributes().getNamedItem("id").getNodeValue());

			// fetch nodes
			NodeList n = ways.item(i).getChildNodes();
			System.out.println("Way: id " + id);

			List<Point> points = new ArrayList<>();
			List<Long> containingNodes = new ArrayList<>();
			boolean closed = false;
			for (int j = 0; j < n.getLength(); j++) {
				if (n.item(j).getNodeName().equals("nd")) {

					String sNodeID = n.item(j).getAttributes().getNamedItem("ref").getNodeValue();
					Long nodeID = Long.valueOf(sNodeID);
					// Test, if path is open
					if (!closed) {
						closed = containingNodes.contains(nodeID);
						containingNodes.add(nodeID);
					}
					// System.out.println("Nodes: " + nodeID);
					try {
						Point point = nodesMap.get(nodeID).getP();
						points.add(point);
					} catch (Exception e) {

					}

				}
			}
			Point[] pointsArray = new Point[points.size()];
			for (int j = 0; j < pointsArray.length; j++) {
				pointsArray[j] = points.get(j);
			}

			LineString path = new LineString(pointsArray);

			// System.out.println("way closed " + closed);

			List<String> keys = new ArrayList<>();
			List<String> values = new ArrayList<>();

			// Tags fetchen
			for (int j = 0; j < n.getLength(); j++) {
				if (n.item(j).getNodeName().equals("tag")) {
					String k = n.item(j).getAttributes().getNamedItem("k").getNodeValue();
					String v = n.item(j).getAttributes().getNamedItem("v").getNodeValue();
					System.out.println("Tags: " + k + " " + v);
					keys.add(k);
					values.add(v);
				}
			}
			MyWay myWay = new MyWay(path, keys, values, closed);
			waysMap.put(id, myWay);
		}
	}

	public void fetchTags() {
		tags = new HashMap<>();
		fetchTags(nodes);
		// fetchTags(ways);
		// fetchTags(relations);
	}

	public void fetchTags(NodeList nl) {
		for (int i = 0; i < nl.getLength(); i++) {
			// for (int i = 0; i < 100; i++) {
			NodeList n = nl.item(i).getChildNodes();
			for (int j = 0; j < n.getLength(); j++)
				if (n.item(j).getNodeName().equals("tag")) {
					String k = n.item(j).getAttributes().getNamedItem("k").getNodeValue();
					String v = n.item(j).getAttributes().getNamedItem("v").getNodeValue();

					String key = "k=" + k + "\tv=" + v;
					Integer val = tags.get(key);
					if (val == null) {
						val = 1;
					} else
						val = val + 1;
					tags.put(key, val);
					// System.out.println(key + " = " + val);
				}
		}
	}

	public void connectToPostgres() throws ClassNotFoundException {
		Class.forName("org.postgresql.Driver");
		String url = "jdbc:postgresql://localhost:5432/geo";
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url, "admin", "admin");

		} catch (SQLException e) {
			try {
				conn = DriverManager.getConnection(url, "postgres", "");
			} catch (SQLException e2) {
				System.out.println("Could not connect to database");
				return;
			}
		}
		List<Long> done = new ArrayList<>();
		int allEntries = nodesMap.size() + waysMap.size();
		boolean checkInsertStable = false;
		int inserts = 0;
		int counter = 1;
		// Mehrfach drüber gehen, wegen Aobhängigkeit
		while (!checkInsertStable){
			
			System.out.println(counter + ". Durchlauf: ");
			done.addAll(fillTablesNodes(conn));
			// Eingefügte löschen
			for(long key : done){
				nodesMap.remove(key);
			}
			done.addAll(fillTablesWays(conn));
			for(long key : done){
				waysMap.remove(key);
			}
			inserts = inserts + done.size();
			if (done.size() == 0){
				checkInsertStable = true;
			}
			System.out.println("---------------------------------------");
			done.clear();
		}
		System.out.println("Done " + inserts + " entrys out of " + (allEntries));
		if (conn != null)
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	private List<Long> fillTablesNodes(Connection conn) {
		// Ampel
		List<Long> done = new ArrayList<>();
		for (long key : nodesMap.keySet()) {
			if (nodesMap.get(key).value.contains("traffic_signals")) {
				if(fillTableAmpel(conn, key))
					done.add(key);
			}
			// Haltestelle
			if (nodesMap.get(key).value.contains("bus_stop") || nodesMap.get(key).value.contains("tram_stop")) {
				if(fillTableHaltestelle(conn, key))
					done.add(key);
			}
			// Haus
			if (nodesMap.get(key).key.contains("addr:housenumber")) {
				if(fillTableHaus(conn, key))
					done.add(key);
			}

		}
		return done;
	}

	private boolean fillTableAmpel(Connection conn, long nodeKey) {
		PreparedStatement ps;
		try {
			ps = conn.prepareStatement("INSERT INTO ampel VALUES (?,?,?,?)");
		} catch (SQLException e) {
			System.out.println("ERROR: Could not create Statement for Node: " + nodeKey + " to table Ampel");
			System.out.println("REASON: " + e.getMessage());
			return false;
		}
		long id = nodeKey;
		MyNode node = nodesMap.get(nodeKey);
		Point point = node.p;
		// sound
		boolean sound = node.key.contains("traffic_signals:sound");
		if (sound) {
			int index = node.key.indexOf("traffic_signals:sound");
			if (node.value.get(index).equals("no"))
				sound = false;
		}
		// vibration
		boolean vib = node.key.contains("traffic_signals:vibration");
		if (vib) {
			int index = node.key.indexOf("traffic_signals:vibration");
			if (node.value.get(index).equals("no"))
				vib = false;
		}

		// setzen des Parameters und ausfuehren der Anweisung
		try {
			ps.setObject(1, id);
			ps.setObject(2, point, java.sql.Types.OTHER);
			ps.setObject(3, sound);
			ps.setObject(4, vib);
			ps.executeUpdate();

		} catch (SQLException e) {
			System.out.println("ERROR: at Node: " + nodeKey + " with statement " + ps.toString());
			if (e.getMessage().contains("ASSERTION") && e.getMessage().contains("potentiel Verletzt"))
				System.out.println("REASON: ASSERTION verletzt");
			else
				System.out.println("REASON: " + e.getMessage());
			return false;
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {
				System.out.println("ERROR: at Node: " + nodeKey + " could not close Statement");
			}
		}

		//System.out.println("Ampel: Insert done");
		return true;
	}

	private boolean fillTableHaltestelle(Connection conn, long nodeKey) {
		PreparedStatement ps;
		try {
			ps = conn.prepareStatement(
					// id pos shelter busroutes name
					"INSERT INTO haltestelle VALUES (?,?,?,?,?)");
		} catch (SQLException e) {
			System.out.println("ERROR: Could not create Statement for Node: " + nodeKey + " to table Haltestelle");
			System.out.println("REASON: " + e.getMessage());
			return false;
		}
		long id = nodeKey;
		MyNode node = nodesMap.get(nodeKey);
		Point point = node.p;
		// shelter
		boolean shelter = node.key.contains("shelter");
		if (shelter) {
			int index = node.key.indexOf("shelter");
			if (node.value.get(index).equals("no"))
				shelter = false;
		}

		String sBusRoutes = getValueOfNodeByString(node, "bus_routes", 50);
		String sName = getValueOfNodeByString(node, "name", 50);
		// setzen des Parameters und ausfuehren der Anweisung
		try {
			ps.setObject(1, id);
			ps.setObject(2, point, java.sql.Types.OTHER);
			ps.setObject(3, shelter);
			ps.setObject(4, sBusRoutes);
			ps.setObject(5, sName);
			ps.executeUpdate();
		} catch (SQLException e) {
			System.out.println("ERROR: at Node: " + nodeKey + " with statement " + ps.toString());
			if (e.getMessage().contains("ASSERTION") && e.getMessage().contains("potentiel Verletzt"))
				System.out.println("REASON: ASSERTION verletzt");
			else
				System.out.println("REASON: " + e.getMessage());
			return false;

		} finally {
			try {
				ps.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//System.out.println("Buststops: Insert done");
		return true;
	}

	/**
	 * Liefrt von einer MyNode zu eieńem bestimmten key den value zurück
	 * 
	 * @param node
	 * @param key
	 * @param length
	 * @return
	 */
	private String getValueOfNodeByString(MyNode node, String key, int length) {
		String value = "";
		boolean contains = node.key.contains(key);
		if (contains) {
			int index = node.key.indexOf(key);
			value = node.value.get(index);
			if (value.length() > length) {
				value = value.substring(0, length - 1);
			}
		}
		return value;
	}

	/**
	 * Liefrt von einer MyWay zu eieńem bestimmten key den value zurück
	 * 
	 * @param way
	 * @param key
	 * @param length
	 * @return
	 */
	private String getValueOfWayByString(MyWay way, String key, int length) {
		String value = "";
		boolean contains = way.key.contains(key);
		if (contains) {
			int index = way.key.indexOf(key);
			value = way.value.get(index);
			if (value.length() > length) {
				value = value.substring(0, length - 1);
			}
		}
		return value;
	}

	private boolean fillTableHaus(Connection conn, long nodeKey) {
		PreparedStatement ps;
		try {
			ps = conn.prepareStatement(
					/*
					 * id bigserial not null, sid bigserial ,-- Haus befindet
					 * sich an Weg name varchar(50) , housenumber int , postcode
					 * int , city varchar(50) , geb_nr int , levels int , height
					 * decimal , roof_shape varchar(50) , amnity varchar(50) ,
					 * shop varchar(50) , tourism varchar(50) , operator
					 * varchar(50) , umriss polygon , pos point
					 */
					"INSERT INTO haus VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
		} catch (SQLException e) {
			System.out.println("ERROR: Could not create Statement for Node: " + nodeKey + " to table Haus");
			System.out.println("REASON: " + e.getMessage());
			return false;
		}
		long id = nodeKey;
		MyNode node = nodesMap.get(nodeKey);

		Point point = node.p;

		String name = getValueOfNodeByString(node, "name", 50);
		String city = getValueOfNodeByString(node, "addr:city", 50);
		String street = getValueOfNodeByString(node, "addr:street", 50);
		String roof_shape = getValueOfNodeByString(node, "building:roof_shape", 50);
		String amnity = getValueOfNodeByString(node, "amnity", 50);
		String shop = getValueOfNodeByString(node, "shop", 50);
		String tourism = getValueOfNodeByString(node, "tourism", 50);
		String operator = getValueOfNodeByString(node, "operator", 50);

		String sHousenumber = getValueOfNodeByString(node, "addr:housenumber", 10);

		String spostcode = getValueOfNodeByString(node, "postal_code", 50);
		if (spostcode.equals("")) {
			spostcode = getValueOfNodeByString(node, "addr:postcode", 50);
		}
		String sgeb_nr = getValueOfNodeByString(node, "geb_nr", 50);
		int geb_nr = 0;
		if (sgeb_nr != "") {
			geb_nr = Integer.parseInt(sgeb_nr);
		}
		String slevels = getValueOfNodeByString(node, "building:levels", 50);
		int levels = 0;
		if (slevels != "") {
			levels = Integer.parseInt(slevels);
		}
		String sheight = getValueOfNodeByString(node, "height", 50);
		double height = 0.0;
		if (sheight != "") {
			height = Double.parseDouble(sheight);
		}
		// setzen des Parameters und ausfuehren der Anweisung
		try {
			ps.setObject(1, id);
			ps.setObject(2, street);
			ps.setObject(3, name);
			ps.setObject(4, sHousenumber);
			ps.setObject(5, spostcode);
			ps.setObject(6, city);
			ps.setObject(7, geb_nr);
			ps.setObject(8, levels);
			ps.setObject(9, height);
			ps.setObject(10, roof_shape);
			ps.setObject(11, amnity);
			ps.setObject(12, shop);
			ps.setObject(13, tourism);
			ps.setObject(14, operator);
			ps.setObject(15, null);
			ps.setObject(16, point, java.sql.Types.OTHER);

			ps.executeUpdate();
		} catch (SQLException e) {
			System.out.println("ERROR: at Node: " + nodeKey + " with statement " + ps.toString());
			System.out.println("REASON: " + e.getMessage());
			return false;

		} finally {
			try {
				ps.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		//System.out.println("Haus: Insert done");
		return true;

	}

	private List<Long> fillTablesWays(Connection conn) {
		List<Long> done = new ArrayList<>();
		for (long key : waysMap.keySet()) {
			// Parkplatz
			if (waysMap.get(key).value.contains("parking")) {
				if (waysMap.get(key).isClosed()) {
					if(fillTableParkplatz(conn, key))
						done.add(key);
				}

			}
			// Haus
			if (waysMap.get(key).key.contains("building")) {
				int index = waysMap.get(key).key.indexOf("building");
				if (waysMap.get(key).value.get(index).contains("yes")) {
					if (waysMap.get(key).isClosed()) {
						if(fillTableHaus2(conn, key))
							done.add(key);
					}
				}
			}
			// Strasse
			if (waysMap.get(key).key.contains("highway")) {
				int index = waysMap.get(key).key.indexOf("highway");
				String value = waysMap.get(key).value.get(index);
				if (value.contains("primary") || value.contains("secondary") || value.contains("tertiary")
						|| value.contains("residential") || value.contains("living_street")
						|| value.contains("unclassified") || value.contains("service")) {
					if(fillTableStreet(conn, key))
						done.add(key);
				}

			}
			// Eisenbahn oder Straßenbahn
			if (waysMap.get(key).key.contains("railway")) {
				int index = waysMap.get(key).key.indexOf("railway");
				if (waysMap.get(key).value.get(index).contains("rail")) {
					if(fillTableEisenbahn(conn, key))
						done.add(key);
				} else if (waysMap.get(key).value.get(index).contains("tram")) {
					if(fillTableStraßenbahn(conn, key))
						done.add(key);
				}
			}
			// Fluss
			if (waysMap.get(key).key.contains("waterway")) {
				int index = waysMap.get(key).key.indexOf("waterway");
				if (waysMap.get(key).value.get(index).contains("river")) {
					if(fillTableFluss(conn, key))
						done.add(key);
				}
			}
			// See
			if (waysMap.get(key).key.contains("natural")) {
				int index = waysMap.get(key).key.indexOf("natural");
				if (waysMap.get(key).value.get(index).contains("water")) {
					if (waysMap.get(key).isClosed()) {
						if(fillTableSee(conn, key))
							done.add(key);
					}
				}
			}
			// Landnutzung
			if (waysMap.get(key).key.contains("landuse")) {
				if (waysMap.get(key).isClosed()) {
					if(fillTableLandnutzung(conn, key))
						done.add(key);
				}

			}
			// Park oder Spielplatz
			if (waysMap.get(key).key.contains("leisure")) {
				if (waysMap.get(key).isClosed()) {
					int index = waysMap.get(key).key.indexOf("leisure");

					if (waysMap.get(key).value.get(index).contains("park")) {
						if(fillTablePark(conn, key))
							done.add(key);
					} else if (waysMap.get(key).value.get(index).contains("playground")) {
						if(fillTableSpielplatz(conn, key))
							done.add(key);
					}
				}
			}
			// Tunnel
			if (waysMap.get(key).key.contains("tunnel")) {
				int index = waysMap.get(key).key.indexOf("tunnel");
				if (waysMap.get(key).value.get(index).contains("yes")) {
					if(fillTableTunnel(conn, key))
						done.add(key);
				}
			}

			// Bruecke
			if (waysMap.get(key).key.contains("bridge")) {
				int index = waysMap.get(key).key.indexOf("bridge");
				if (waysMap.get(key).value.get(index).contains("yes")) {
					if(fillTableBruecke(conn, key))
						done.add(key);
				}
			}
		}
		return done;
	}

	private boolean fillTableParkplatz(Connection conn, long nodeKey) {
		PreparedStatement ps;
		try {
			ps = conn.prepareStatement(
					/*
					 * id bigserial not null, access boolean , fee boolean ,
					 * capicity int , operator varchar (50) , umriss polygon ,
					 * name varchar(50) ,
					 */
					"INSERT INTO parkplatz VALUES (?,?,?,?,?,?,?)");
		} catch (SQLException e) {
			System.out.println("ERROR: Could not create Statement for Node: " + nodeKey + " to table Parkplatz");
			System.out.println("REASON: " + e.getMessage());
			return false;
		}
		long id = nodeKey;
		MyWay way = waysMap.get(nodeKey);
		LinearRing rings[] = { new LinearRing((way.path.getPoints())) };
		Polygon polygon = new Polygon(rings);
		// access
		boolean access = way.key.contains("access");
		if (access) {
			int index = way.key.indexOf("access");
			if (way.value.get(index).equals("no"))
				access = false;
		}
		// fee
		boolean fee = way.key.contains("fee");
		if (fee) {
			int index = way.key.indexOf("fee");
			if (way.value.get(index).equals("no"))
				fee = false;
		}
		String name = getValueOfWayByString(way, "name", 50);
		String operator = getValueOfWayByString(way, "operator", 50);
		String scapicity = getValueOfWayByString(way, "capicity", 50);
		int capicity = 0;
		if (scapicity != "") {
			capicity = Integer.parseInt(scapicity);
		}
		// setzen des Parameters und ausfuehren der Anweisung
		try {
			ps.setObject(1, id);
			ps.setObject(2, access);
			ps.setObject(3, fee);
			ps.setObject(4, capicity);
			ps.setObject(5, operator);
			ps.setObject(6, polygon, java.sql.Types.OTHER);
			ps.setObject(7, name);

			ps.executeUpdate();
		} catch (SQLException e) {
			System.out.println("ERROR: at Node: " + nodeKey + " with statement " + ps.toString());
			System.out.println("REASON: " + e.getMessage());
			return false;
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {
				System.out.println("ERROR: at Node: " + nodeKey + " could not close Statement");
			}
		}
		//System.out.println("Parkplatz: Insert done");
		return true;

	}

	private boolean fillTableHaus2(Connection conn, long nodeKey) {
		PreparedStatement ps;
		try {
			ps = conn.prepareStatement(
					/*
					 * id bigserial not null, sid bigserial ,-- Haus befindet
					 * sich an Weg name varchar(50) , housenumber int , postcode
					 * int , city varchar(50) , geb_nr int , levels int , height
					 * decimal , roof_shape varchar(50) , amnity varchar(50) ,
					 * shop varchar(50) , tourism varchar(50) , operator
					 * varchar(50) , umriss polygon , pos point
					 */
					"INSERT INTO haus VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
		} catch (SQLException e) {
			System.out.println("ERROR: Could not create Statement for Node: " + nodeKey + " to table Haus");
			System.out.println("REASON: " + e.getMessage());
			return false;
		}
		long id = nodeKey;
		MyWay way = waysMap.get(nodeKey);

		LinearRing rings[] = { new LinearRing((way.path.getPoints())) };
		Polygon polygon = new Polygon(rings);

		String name = getValueOfWayByString(way, "name", 50);
		String city = getValueOfWayByString(way, "addr:city", 50);
		String roof_shape = getValueOfWayByString(way, "building:roof_shape", 50);
		String street = getValueOfWayByString(way, "addr:street", 50);
		String amnity = getValueOfWayByString(way, "amnity", 50);
		String shop = getValueOfWayByString(way, "shop", 50);
		String tourism = getValueOfWayByString(way, "tourism", 50);
		String operator = getValueOfWayByString(way, "operator", 50);

		String sHousenumber = getValueOfWayByString(way, "addr:housenumber", 10);

		String spostcode = getValueOfWayByString(way, "postal_code", 50);
		if (spostcode.equals("")) {
			spostcode = getValueOfWayByString(way, "addr:postcode", 50);
		}
		String sgeb_nr = getValueOfWayByString(way, "geb_nr", 50);
		int geb_nr = 0;
		if (sgeb_nr != "") {
			geb_nr = Integer.parseInt(sgeb_nr);
		}
		String slevels = getValueOfWayByString(way, "building:levels", 50);
		int levels = 0;
		if (slevels != "") {
			levels = Integer.parseInt(slevels);
		}
		String sheight = getValueOfWayByString(way, "height", 50);
		double height = 0.0;
		if (sheight != "") {
			height = Double.parseDouble(sheight);
		}
		// setzen des Parameters und ausfuehren der Anweisung
		try {
			ps.setObject(1, id);
			ps.setObject(2, street);
			ps.setObject(3, name);
			ps.setObject(4, sHousenumber);
			ps.setObject(5, spostcode);
			ps.setObject(6, city);
			ps.setObject(7, geb_nr);
			ps.setObject(8, levels);
			ps.setObject(9, height);
			ps.setObject(10, roof_shape);
			ps.setObject(11, amnity);
			ps.setObject(12, shop);
			ps.setObject(13, tourism);
			ps.setObject(14, operator);
			ps.setObject(15, polygon, java.sql.Types.OTHER);
			ps.setObject(16, null);

			ps.executeUpdate();
		} catch (SQLException e) {
			System.out.println("ERROR: at Node: " + nodeKey + " with statement " + ps.toString());
			System.out.println("REASON: " + e.getMessage());
			return false;
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {
				System.out.println("ERROR: at Node: " + nodeKey + " could not close Statement");
			}
		}

	//	System.out.println("Haus: Insert done");
		return true;

	}

	private boolean fillTableStreet(Connection conn, long nodeKey) {
		PreparedStatement ps;
		try {
			ps = conn.prepareStatement(
					/*
					 * id bigserial not null, name varchar(50) , oneway boolean
					 * , maxspeed int , surface varchar(50) , lanes int , path
					 * path ,
					 */
					"INSERT INTO strasse VALUES (?,?,?,?,?,?,?)");
		} catch (SQLException e1) {
			System.out.println("ERROR: Could not create Statement for Node: " + nodeKey + " to table Street");
			System.out.println("REASON: " + e1.getMessage());
			return false;
		}
		long id = nodeKey;
		MyWay way = waysMap.get(nodeKey);
		LineString path = way.path;
		String name = getValueOfWayByString(way, "name", 50);
		String surface = getValueOfWayByString(way, "surface", 50);
		String slanes = getValueOfWayByString(way, "lanes", 50);
		int lanes = 0;
		if (slanes != "") {
			lanes = Integer.parseInt(slanes);
		}
		String smaxspeed = getValueOfWayByString(way, "maxspeed", 50);
		int maxspeed = 0;
		if (smaxspeed != "") {
			maxspeed = Integer.parseInt(smaxspeed);
		}
		// oneway
		boolean oneway = way.key.contains("oneway");
		if (oneway) {
			int index = way.key.indexOf("oneway");
			if (way.value.get(index).equals("no"))
				oneway = false;
		}

		// setzen des Parameters und ausfuehren der Anweisung
		try {
			ps.setObject(1, id);
			ps.setObject(2, name);
			ps.setObject(3, oneway);
			ps.setObject(4, maxspeed);
			ps.setObject(5, surface);
			ps.setObject(6, lanes);
			ps.setObject(7, path, java.sql.Types.OTHER);
			ps.executeUpdate();
		} catch (SQLException e) {
			System.out.println("ERROR: at Node: " + nodeKey + " with statement " + ps.toString());
			System.out.println("REASON: " + e.getMessage());
			return false;
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {
				System.out.println("ERROR: at Node: " + nodeKey + " could not close Statement");
			}
		}
		//System.out.println("Straße: Insert done");
		return true;

	}

	private boolean fillTableStraßenbahn(Connection conn, long nodeKey) {
		PreparedStatement ps;
		try {
			ps = conn.prepareStatement(
					/*
					 * id bigserial not null, path path
					 */
					"INSERT INTO strassenbahn VALUES (?,?)");
		} catch (SQLException e) {
			System.out.println("ERROR: Could not create Statement for Node: " + nodeKey + " to table Straßenbahn");
			System.out.println("REASON: " + e.getMessage());
			return false;
		}
		long id = nodeKey;
		MyWay way = waysMap.get(nodeKey);
		LineString path = way.path;

		// setzen des Parameters und ausfuehren der Anweisung
		try {
			ps.setObject(1, id);
			ps.setObject(2, path, java.sql.Types.OTHER);
			ps.executeUpdate();
		} catch (SQLException e) {
			System.out.println("ERROR: at Node: " + nodeKey + " with statement " + ps.toString());
			System.out.println("REASON: " + e.getMessage());
			return false;
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {
				System.out.println("ERROR: at Node: " + nodeKey + " could not close Statement");
			}
		}
		//System.out.println("Straßenbahn: Insert done");
		return true;
	}

	private boolean fillTableEisenbahn(Connection conn, long nodeKey) {
		PreparedStatement ps;
		try {
			ps = conn.prepareStatement(
					/*
					 * id bigserial not null, path path
					 */
					"INSERT INTO eisenbahn VALUES (?,?)");
		} catch (SQLException e1) {
			System.out.println("ERROR: Could not create Statement for Node: " + nodeKey + " to table Eisenbahn");
			System.out.println("REASON: " + e1.getMessage());
			return false;
		}
		long id = nodeKey;
		MyWay way = waysMap.get(nodeKey);
		LineString path = way.path;

		// setzen des Parameters und ausfuehren der Anweisung
		try {
			ps.setObject(1, id);
			ps.setObject(2, path, java.sql.Types.OTHER);
			ps.executeUpdate();
		} catch (SQLException e) {
			System.out.println("ERROR: at Node: " + nodeKey + " with statement " + ps.toString());
			System.out.println("REASON: " + e.getMessage());
			return false;
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {
				System.out.println("ERROR: at Node: " + nodeKey + " could not close Statement");
			}
		}

		//System.out.println("Eisenbahn: Insert done");
		return true;

	}

	private boolean fillTableFluss(Connection conn, long nodeKey) {
		PreparedStatement ps;
		try {
			ps = conn.prepareStatement(
					/*
					 * id bigserial not null, path path
					 */
					"INSERT INTO fluss VALUES (?,?,?)");
		} catch (SQLException e1) {
			System.out.println("ERROR: Could not create Statement for Node: " + nodeKey + " to table Fluss");
			System.out.println("REASON: " + e1.getMessage());
			return false;
		}
		long id = nodeKey;
		MyWay way = waysMap.get(nodeKey);
		LineString path = way.path;
		String name = getValueOfWayByString(way, "name", 50);
		// setzen des Parameters und ausfuehren der Anweisung
		try {
			ps.setObject(1, id);
			ps.setObject(2, name);
			ps.setObject(3, path, java.sql.Types.OTHER);
			ps.executeUpdate();
		} catch (SQLException e) {
			System.out.println("ERROR: at Node: " + nodeKey + " with statement " + ps.toString());
			System.out.println("REASON: " + e.getMessage());
			return false;
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {
				System.out.println("ERROR: at Node: " + nodeKey + " could not close Statement");
			}
		}

		//System.out.println("Fluss: Insert done");
		return true;

	}

	private boolean fillTableSee(Connection conn, long nodeKey) {
		PreparedStatement ps;
		try {
			ps = conn.prepareStatement(
					/*
					 * id bigserial not null, umriss polygon , name varchar(50)
					 */
					"INSERT INTO see VALUES (?,?,?)");
		} catch (SQLException e1) {
			System.out.println("ERROR: Could not create Statement for Node: " + nodeKey + " to table See");
			System.out.println("REASON: " + e1.getMessage());
			return false;
		}
		long id = nodeKey;
		MyWay way = waysMap.get(nodeKey);
		LinearRing rings[] = { new LinearRing((way.path.getPoints())) };
		Polygon polygon = new Polygon(rings);
		String name = getValueOfWayByString(way, "name", 50);
		// setzen des Parameters und ausfuehren der Anweisung
		try {
			ps.setObject(1, id);
			ps.setObject(2, polygon, java.sql.Types.OTHER);
			ps.setObject(3, name);
			ps.executeUpdate();
		} catch (SQLException e) {
			System.out.println("ERROR: at Node: " + nodeKey + " with statement " + ps.toString());
			System.out.println("REASON: " + e.getMessage());
			return false;
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {
				System.out.println("ERROR: at Node: " + nodeKey + " could not close Statement");
			}
		}

		//System.out.println("See: Insert done");
		return true;

	}

	private boolean fillTableLandnutzung(Connection conn, long nodeKey) {
		PreparedStatement ps;
		try {
			ps = conn.prepareStatement(
					/*
					 * id bigserial not null, umriss polygon , name varchar(50)
					 */
					"INSERT INTO landnutzung VALUES (?,?,?)");
		} catch (SQLException e1) {
			System.out.println("ERROR: Could not create Statement for Node: " + nodeKey + " to table Landnutzung");
			System.out.println("REASON: " + e1.getMessage());
			return false;
		}
		long id = nodeKey;
		MyWay way = waysMap.get(nodeKey);
		LinearRing rings[] = { new LinearRing((way.path.getPoints())) };
		Polygon polygon = new Polygon(rings);
		String name = getValueOfWayByString(way, "landuse", 50);
		// setzen des Parameters und ausfuehren der Anweisung
		try {
			ps.setObject(1, id);
			ps.setObject(2, polygon, java.sql.Types.OTHER);
			ps.setObject(3, name);
			ps.executeUpdate();
		} catch (SQLException e) {
			System.out.println("ERROR: at Node: " + nodeKey + " with statement " + ps.toString());
			System.out.println("REASON: " + e.getMessage());
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {
				System.out.println("ERROR: at Node: " + nodeKey + " could not close Statement");
			}
		}

		//System.out.println("Landnutzung: Insert done");
		return true;

	}

	private boolean fillTablePark(Connection conn, long nodeKey) {
		PreparedStatement ps;
		try {
			ps = conn.prepareStatement(
					/*
					 * id bigserial not null, umriss polygon , name varchar(50)
					 */
					"INSERT INTO park VALUES (?,?,?)");
		} catch (SQLException e1) {
			System.out.println("ERROR: Could not create Statement for Node: " + nodeKey + " to table Park");
			System.out.println("REASON: " + e1.getMessage());
			return false;
		}
		long id = nodeKey;
		MyWay way = waysMap.get(nodeKey);
		LinearRing rings[] = { new LinearRing((way.path.getPoints())) };
		Polygon polygon = new Polygon(rings);
		String name = getValueOfWayByString(way, "name", 50);
		// setzen des Parameters und ausfuehren der Anweisung
		try {
			ps.setObject(1, id);
			ps.setObject(2, polygon, java.sql.Types.OTHER);
			ps.setObject(3, name);
			ps.executeUpdate();
		} catch (SQLException e) {
			System.out.println("ERROR: at Node: " + nodeKey + " with statement " + ps.toString());
			System.out.println("REASON: " + e.getMessage());
			return false;
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {
				System.out.println("ERROR: at Node: " + nodeKey + " could not close Statement");
			}
		}

		//System.out.println("Park: Insert done");
		return true;

	}

	private boolean fillTableSpielplatz(Connection conn, long nodeKey) {
		PreparedStatement ps;
		try {
			ps = conn.prepareStatement(
					/*
					 * id bigserial not null, umriss polygon , name varchar(50)
					 */
					"INSERT INTO spielplatz VALUES (?,?,?)");
		} catch (SQLException e1) {
			System.out.println("ERROR: Could not create Statement for Node: " + nodeKey + " to table Spielplatz");
			System.out.println("REASON: " + e1.getMessage());
			return false;
		}
		long id = nodeKey;
		MyWay way = waysMap.get(nodeKey);
		LinearRing rings[] = { new LinearRing((way.path.getPoints())) };
		Polygon polygon = new Polygon(rings);
		String name = getValueOfWayByString(way, "name", 50);
		// setzen des Parameters und ausfuehren der Anweisung
		try {
			ps.setObject(1, id);
			ps.setObject(2, polygon, java.sql.Types.OTHER);
			ps.setObject(3, name);
			ps.executeUpdate();
		} catch (SQLException e) {
			System.out.println("ERROR: at Node: " + nodeKey + " with statement " + ps.toString());
			System.out.println("REASON: " + e.getMessage());
			return false;
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {
				System.out.println("ERROR: at Node: " + nodeKey + " could not close Statement");
			}
		}

		//System.out.println("Spielplatz: Insert done");
		return true;

	}

	private boolean fillTableTunnel(Connection conn, long nodeKey) {

		PreparedStatement ps;
		try {
			ps = conn.prepareStatement(
					/*
					 * id bigserial not null, name varchar(50) , oneway boolean
					 * , maxspeed int , surface varchar(50) , lanes int , road
					 * boolean , river boolean , rail boolean ,path path ,
					 */
					"INSERT INTO tunnel VALUES (?,?,?,?,?,?,?,?,?,?)");
		} catch (SQLException e1) {
			System.out.println("ERROR: Could not create Statement for Node: " + nodeKey + " to table Straßenbahn");
			System.out.println("REASON: " + e1.getMessage());
			return false;

		}
		long id = nodeKey;
		MyWay way = waysMap.get(nodeKey);
		LineString path = way.path;
		String name = getValueOfWayByString(way, "name", 50);
		String surface = getValueOfWayByString(way, "surface", 50);
		String slanes = getValueOfWayByString(way, "lanes", 50);
		int lanes = 0;
		if (slanes != "") {
			lanes = Integer.parseInt(slanes);
		}
		String smaxspeed = getValueOfWayByString(way, "maxspeed", 50);
		int maxspeed = 0;
		if (smaxspeed != "") {
			maxspeed = Integer.parseInt(smaxspeed);
		}
		// oneway
		boolean oneway = way.key.contains("oneway");
		if (oneway) {
			int index = way.key.indexOf("oneway");
			if (way.value.get(index).equals("no"))
				oneway = false;
		}
		// rail
		boolean rail = false;
		int index = way.key.indexOf("railway");
		if (index != -1 && way.value.get(index).equals("rail"))
			rail = true;
		// river
		boolean river = false;
		index = way.key.indexOf("waterway");
		if (index != -1 && way.value.get(index).equals("river"))
			river = true;
		// road
		boolean road = false;
		index = way.key.indexOf("highway");
		if (index != -1) {
			String value = way.value.get(index);
			if ((value.contains("primary") || value.contains("secondary") || value.contains("tertiary")
					|| value.contains("residential") || value.contains("living_street")
					|| value.contains("unclassified") || value.contains("service"))) {
				road = true;
			}
		}
		// setzen des Parameters und ausfuehren der Anweisung
		try {
			ps.setObject(1, id);
			ps.setObject(2, name);
			ps.setObject(3, oneway);
			ps.setObject(4, maxspeed);
			ps.setObject(5, surface);
			ps.setObject(6, lanes);
			ps.setObject(7, road);
			ps.setObject(8, river);
			ps.setObject(9, rail);
			ps.setObject(10, path, java.sql.Types.OTHER);
			ps.executeUpdate();
		} catch (SQLException e) {
			System.out.println("ERROR: at Node: " + nodeKey + " with statement " + ps.toString());
			System.out.println("REASON: " + e.getMessage());
			return false;
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {
				System.out.println("ERROR: at Node: " + nodeKey + " could not close Statement");
			}
		}

		//System.out.println("Bruecke: Insert done");
		return true;

	}

	private boolean fillTableBruecke(Connection conn, long nodeKey) {
		PreparedStatement ps;
		try {
			ps = conn.prepareStatement(
					/*
					 * id bigserial not null, name varchar(50) , oneway boolean
					 * , maxspeed int , surface varchar(50) , lanes int , road
					 * boolean , rail boolean ,path path ,
					 */
					"INSERT INTO bruecke VALUES (?,?,?,?,?,?,?,?,?)");
		} catch (SQLException e1) {
			System.out.println("ERROR: Could not create Statement for Node: " + nodeKey + " to table Straßenbahn");
			System.out.println("REASON: " + e1.getMessage());
			return false;
		}
		long id = nodeKey;
		MyWay way = waysMap.get(nodeKey);
		LineString path = way.path;
		String name = getValueOfWayByString(way, "name", 50);
		String surface = getValueOfWayByString(way, "surface", 50);
		String slanes = getValueOfWayByString(way, "lanes", 50);
		int lanes = 0;
		if (slanes != "") {
			lanes = Integer.parseInt(slanes);
		}
		String smaxspeed = getValueOfWayByString(way, "maxspeed", 50);
		int maxspeed = 0;
		if (smaxspeed != "") {
			maxspeed = Integer.parseInt(smaxspeed);
		}
		// oneway
		boolean oneway = way.key.contains("oneway");
		if (oneway) {
			int index = way.key.indexOf("oneway");
			if (way.value.get(index).equals("no"))
				oneway = false;
		}
		// rail
		boolean rail = false;

		int index = way.key.indexOf("railway");
		if (index != -1 && way.value.get(index).equals("rail"))
			rail = true;

		// rail
		boolean road = false;
		index = way.key.indexOf("highway");
		if (index != -1) {
			String value = way.value.get(index);
			if ((value.contains("primary") || value.contains("secondary") || value.contains("tertiary")
					|| value.contains("residential") || value.contains("living_street")
					|| value.contains("unclassified") || value.contains("service"))) {
				road = true;
			}
		}
		// setzen des Parameters und ausfuehren der Anweisung
		try {

			ps.setObject(1, id);
			ps.setObject(2, name);
			ps.setObject(3, oneway);
			ps.setObject(4, maxspeed);
			ps.setObject(5, surface);
			ps.setObject(6, lanes);
			ps.setObject(7, road);
			ps.setObject(8, rail);
			ps.setObject(9, path, java.sql.Types.OTHER);
			ps.executeUpdate();

		} catch (SQLException e) {
			System.out.println("ERROR: at Node: " + nodeKey + " with statement " + ps.toString());
			System.out.println("REASON: " + e.getMessage());
			return false;
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {
				System.out.println("ERROR: at Node: " + nodeKey + " could not close Statement");
			}
		}

		//System.out.println("Bruecke: Insert done");
		return true;

	}
}
