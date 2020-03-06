package jscl.editor;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import org.jdesktop.swingx.JXGraph;

public class Graph extends JXGraph {
	public Graph(final Plot ... plots) {
		setMinorCountX(3);
		setMinorCountY(3);
		setView(new Rectangle2D.Double(-1.1, -1.1, 2.2, 2.2));
		addPlots(Color.black, plots);
	}
}
