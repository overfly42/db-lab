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
		for (PGgeometry geo : lg)
			switch (geo.getGeoType()) {
			case Geometry.LINESTRING:
				paintLineString(g, geo, ll.get(lg.indexOf(geo)));
				break;
			case Geometry.POLYGON:
				paintPolygon(g, geo, ll.get(lg.indexOf(geo)));
				break;
			default:

			}
		entryToShow++;
		entryToShow %= objects.size();
	}

	private void paintLineString(Graphics g, PGgeometry geo, long id) {
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
		//Get the relevant Elements
		int[] max = new int[dims];
		for(int i = 0; i < dims;i++)
			max[i]=0;
		int relevant = 100000;
		for (int i = 0; i < coordD.length; i++) {
			for (int n = 0; n < dims; n++) {
				coordI[n][i] = ((int) (relevant*coordD[n][i]))%1000;
				max[n] = Math.max(max[n], coordI[n][i]);
				System.out.print(coordI[n][i]+" ");
				
			}
			System.out.println();
		}
		System.out.println(this.getWidth() + " " + this.getHeight()+" max: "+max[0]+"/"+max[1]);

		 g.drawPolygon(coordI[0], coordI[1], coordS.length);
	}

	private void paintPolygon(Graphics g, PGgeometry geo, long id) {
		System.out.println("Painting a polygon");
	}

}
