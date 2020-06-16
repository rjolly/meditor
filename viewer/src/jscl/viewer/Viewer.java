package jscl.viewer;

import java.applet.Applet;
import java.applet.AppletStub;
import java.applet.AppletContext;
import java.awt.Panel;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.lang.reflect.Method;
import java.net.URL;

public class Viewer extends Panel implements AppletStub {
	public Viewer(String str) {
		this.str = str;
		setLayout(new BorderLayout());
		try {
			a = (Applet)Class.forName("jvLite").newInstance();
			base = new URL("http://www.javaview.de/");
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		a.setPreferredSize(new Dimension(400, 400));
		a.setStub(this);
		a.init();
		a.start();
		add(a);
	}

	public boolean isActive() {
		return true;
	}

	public URL getDocumentBase() {
		return base;
	}

	public URL getCodeBase() {
		return base;
	}

	public String getParameter(String name) {
		if(name.equals("Axes")) {
			return "show";
		}
		if(name.equals("mathematica")) {
			return str;
		}
		return null;
	}

	public AppletContext getAppletContext() {
		return null;
	}

	public void appletResize(int i, int j) {
	}

	public void print(Graphics g) {
		try {
			Class.forName("f").getMethod("ld", new Class[] {
				Graphics.class
			}).invoke(a.getComponent(0), new Object[] {
				g
			});
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	String str;
	Applet a;
	URL base;
}
