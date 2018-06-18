package jscl.editor;

import java.lang.reflect.Method;
import org.jdesktop.swingx.JXGraph;

class Plot extends JXGraph.Plot {
	final Object obj;
	final Method method;

	Plot(final Object obj, final Method method) {
		this.obj = obj;
		this.method = method;
	}

	public double compute(final double x) {
		try {
			return (Double) method.invoke(obj, Double.valueOf(x));
		} catch (final Exception ex) {
			return 0.0;
		}
	}
}
