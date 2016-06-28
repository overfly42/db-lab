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

	final int dims = 2;
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
		System.out.println("=================================");
		System.out.println(objects.size() + " Entrys to display, displaying Entry " + entryToShow);
		PaintingObject po = objects.get(entryToShow);
		List<PGgeometry> lg = new ArrayList<>();
		List<Long> ll = new ArrayList<>();
		lg.add(po.geoA);
		lg.add(po.geoB);
		ll.add(po.idA);
		ll.add(po.idB);
		List<PaintableElement> lpe = new ArrayList<>();
		for (PGgeometry geo : lg) {
			PaintableElement pe = new PaintableElement();
			lpe.add(pe);
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
		paintElements(g, lpe);
		paintNaming(g, lpe.get(0).coord, po.idA);
		paintNaming(g, lpe.get(1).coord, po.idB);
		entryToShow++;
		entryToShow %= objects.size();
	}

	private double[][] calcLineString(PGgeometry geo) {
		System.out.println("Painting a lineString");
		String s = geo.toString();
		// System.out.println(s);
		s = s.substring(s.indexOf('('));
		// System.out.println(s);
		s = s.substring(1);
		// System.out.println(s);
		s = s.substring(0, s.length() - 1);
		System.out.println(s);
		return extractPoints(s);
	}

	private double[][] calcPolygon(PGgeometry geo) {
		System.out.println("Painting a polygon");
		String s = geo.toString();
		s = s.substring(s.indexOf('(') + 1);
		s = s.substring(s.indexOf('(') + 1);
		s = s.substring(0, s.length() - 2);
		System.out.println(s);
		return extractPoints(s);
	}

	private double[][] extractPoints(String s) {
		String[] coordS = s.split(",");
		System.out.println("Coordinates: " + coordS.length);
		double[][] coordD = new double[dims][coordS.length];
		int nonZeros = 0;// All coordinates have a value of >50 or >10
		for (int i = 0; i < coordD[0].length; i++) {
			String[] s2 = coordS[i].split(" ");
			for (int n = 0; n < dims; n++) {
				coordD[n][i] = Double.parseDouble(s2[n]);
				// System.out.println("Got "+ coordD[n][i]+ " from "+ s2[n]);
			}
			if (coordD[0][i] > 0)
				nonZeros++;
		}
		System.out.println("Non Sero Values: " + nonZeros + " of " + coordS.length);
		// Check for sero's
		double[][] coordD2 = new double[dims][nonZeros];
		for (int i = 0; i < nonZeros; i++)
			for (int n = 0; n < dims; n++)
				coordD2[n][i] = coordD[n][i];
		return coordD2;
	}

	private void paintElements(Graphics g, List<PaintableElement> lpe) {
		// First, get x,y minimum and remove it
		// second get x,y maximum
		double[] min = { 10000.0, 10000.0 };
		double[] max = { 0.0, 0.0 };
		for (PaintableElement pe : lpe) {
			if (pe.coord.length == 0)
				continue;
			int elements = pe.coord[0].length;
			for (int i = 0; i < elements; i++)
				for (int n = 0; n < 2; n++) {// Only two dimensions are
												// supportet
					min[n] = Math.min(min[n], pe.coord[n][i]);
					max[n] = Math.max(max[n], pe.coord[n][i]);
				}
		}
		for (int i = 0; i < 2; i++) {
			System.out.println("Min: " + min[i] + " / Max: " + max[i]);
		}
		// Move
		for (PaintableElement pe : lpe) {
			if (pe.coord.length == 0)
				continue;
			int elements = pe.coord[0].length;
			for (int i = 0; i < elements; i++)
				for (int n = 0; n < 2; n++) {
					// System.out.print(pe.coord[n][i] + "->");
					pe.coord[n][i] -= min[n];
					// System.out.println(pe.coord[n][i]);
				}
		}
		// Getting printable area
		int gap = 50;
		int[] dim = { this.getWidth() - 2 * gap, this.getHeight() - 2 * gap };
		double[] dimPx = new double[2];
		for (int i = 0; i < 2; i++) {
			dimPx[i] = dim[i] / (max[i] - min[i]);
			// System.out.println("dimPx: " + dimPx[i]);
			System.out.println(dimPx[i] + " = " + dim[i] + " / " + max[i]);
		}
		// Modify all Data
		for (PaintableElement pe : lpe)
			if (pe.coord.length == 0)
				continue;
			else {
				int[][] coordI = new int[2][pe.coord[0].length];
				for (int i = 0; i < pe.coord[0].length; i++)
					for (int n = 0; n < 2; n++) {
						// System.out.print(pe.coord[n][i] + "->");
						pe.coord[n][i] = (pe.coord[n][i] * dimPx[n]) + gap;
						coordI[n][i] = (int) pe.coord[n][i];
						// System.out.println(pe.coord[n][i]);
					}
				// Paint modifyed data
				switch (pe.getype) {
				case Geometry.LINESTRING:
					g.drawPolyline(coordI[0], coordI[1], coordI[0].length);
					break;
				case Geometry.POLYGON:
					System.out.println("Painted Polygon");
					g.drawPolygon(coordI[0], coordI[1], coordI[0].length);
					break;
				default:
				}
			}
	}

	private void paintNaming(Graphics g, double[][] coordD, long id) {
		double[] mid = { 0.0, 0.0 };
		for (int i = 0; i < coordD[0].length; i++)
			for (int n = 0; n < dims; n++)
				mid[n] += coordD[n][i] / coordD[n].length;
		g.drawString("" + id, (int) mid[0], (int) mid[1]);
	}
}
