package jscl.editor;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.lang.reflect.Method;

public class Graph extends Component {
	Object obj;
	Method method;
	int w = getWidth();
	int h = getHeight();
	double z = 1.0;
	double x0 = 0.0;
	double y0 = 0.0;

	void shift(double x, double y) {
		x0 += x / (double)h * 2.0 / z;
		y0 += y / (double)h * 2.0 / z;
	}

	void scale(double a) {
		z /= a;
	}

	ComponentAdapter cadapter = new ComponentAdapter() {
		public void componentResized(ComponentEvent e) {
			w = getWidth();
			h = getHeight();
			repaint();
		}
	};

	MouseAdapter madapter = new MouseAdapter() {
		int x1 = 0;
		int y1 = 0;

		public void mouseDragged(MouseEvent e) {
			shift(e.getX() - x1, e.getY() - y1);
			repaint();
			x1 = e.getX();
			y1 = e.getY();
		}

		public void mouseMoved(MouseEvent e) {
			x1 = e.getX();
			y1 = e.getY();
		}

		public void mouseWheelMoved(MouseWheelEvent e) {
			scale(Math.pow(2.0, e.getWheelRotation()));
			repaint();
		}
	};

	public Graph(Object obj, Method method) {
		this.obj = obj;
		this.method = method;
		addComponentListener(cadapter);
		addMouseMotionListener(madapter);
		addMouseWheelListener(madapter);
	}

	double eval(double x) {
		try {
			return (Double)method.invoke(obj, new Object[] {x});
		} catch (Exception ex) {
			return 0.0;
		}
	}

	public void paint(Graphics g) {
		int xs[] = new int[w];
		int ys[] = new int[w];
		for (int n = 0 ; n < w ; n++) {
			xs[n] = n;
			double x = (2.0 * (double)n - (double)w) / (double)h / z - x0;
			double y = eval(x);
			ys[n] = (int)(((double)h + (y0 - y) * z * (double)h) / 2.0);
		}
		int x2 = (int)(((double)w + x0 * z * (double)h) / 2.0);
		int y2 = (int)(((double)h + y0 * z * (double)h) / 2.0);
		Color color = g.getColor();
		g.setColor(Color.RED);
		g.drawLine(0, y2, w, y2);
		g.drawLine(x2, 0, x2, h);
		g.setColor(color);
		g.drawPolyline(xs, ys, w);
	}
}
