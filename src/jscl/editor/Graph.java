package jscl.editor;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.util.function.DoubleUnaryOperator;
import org.jdesktop.swingx.JXGraph;

public class Graph extends JXGraph {
	public Graph(final DoubleUnaryOperator ... functions) {
		setMinorCountX(3);
		setMinorCountY(3);
		setView(new Rectangle2D.Double(-1.1, -1.1, 2.2, 2.2));
		addPlots(Color.black, Stream.of(functions).map(function -> new JXGraph.Plot() {
			public double compute(final double x) {
				return function.applyAsDouble(x);
			}
		}).collect(Collectors.toList()).toArray(new JXGraph.Plot[0]));
	}
}
