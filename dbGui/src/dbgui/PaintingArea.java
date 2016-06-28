package dbgui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.postgis.Geometry;
import org.postgis.PGgeometry;

public class PaintingArea extends JPanel {

	public class PaintableElement {
		double[][] coord = null;
		int getype = 0;
	}

	int entryToShow;
	List<PaintingObject> objects;

	public PaintingArea() {
		entryToShow = 0;
		objects = new ArrayList<>();
		this.setPreferredSize(new Dimension(500, 500));
		this.setOpaque(true);
		this.setBackground(Color.CYAN);
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		if (objects.size() <= entryToShow)
			return;
		System.out.println(objects.size() + " Entrys to display, displaying Entry " + entryToShow);
		PaintingObject po = objects.get(entryToShow);
		List<PGgeometry> lg = new ArrayList<>();
		List<Long> ll = new ArrayList<>();
		lg.add(po.geoA);
		lg.add(po.geoB);
		ll.add(po.idA);
		ll.add(po.idB);
		List<PaintingArea> lpa = new ArrayList<>();
		for (PGgeometry geo : lg) {
			PaintableElement pe = new PaintableElement();
			pe.getype = geo.getGeoType();
			switch (geo.getGeoType()) {
			case Geometry.LINESTRING:
				pe.coord = calcLineString(geo);
				break;
			case Geometry.POLYGON:
				pe.coord = calcPolygon(geo);
				break;
			default:

			}
		}
		entryToShow++;
		entryToShow %= objects.size();
	}

	private double[][] calcLineString(PGgeometry geo) {
		final int dims = 2;
		System.out.println("Painting a lineString");
		String s = geo.toString();
		System.out.println(s);
		s = s.substring(s.indexOf('('));
		System.out.println(s);
		s = s.substring(1);
		System.out.println(s);
		s = s.substring(0, s.length() - 1);
		System.out.println(s);
		String[] coordS = s.split(",");
		System.out.println("Coordinates: " + coordS.length);
		double[][] coordD = new double[dims][coordS.length];
		int[][] coordI = new int[dims][coordS.length];
		for (int i = 0; i < coordD.length; i++) {
			String[] s2 = coordS[i].split(" ");
			for (int n = 0; n < dims; n++) {
				coordD[n][i] = Double.parseDouble(s2[n]);
			}
		}
		return coordD;
	}

	private double[][] calcPolygon(PGgeometry geo) {
		System.out.println("Painting a polygon");
		double[][] coordD = new double[0][0];
		return coordD;
	}

}
