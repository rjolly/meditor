/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jscl.editor;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ServiceLoader;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 *
 * @author raphael
 */
public class MathManager {
	private static final MathManager manager = new MathManager();
	private final Preferences pref = NbPreferences.forModule(MathManager.class);
	private ScriptEngine engine;
	private URL location;
	private String stylesheet;
	private boolean rendering;

	public MathManager() {
		setEngine(pref.get("engine", ""));
		setLocation(pref.get("location", ""));
		setRendering(pref.get("rendering", ""));

		pref.addPreferenceChangeListener(new PreferenceChangeListener() {
			public void preferenceChange(PreferenceChangeEvent evt) {
				if (evt.getKey().equals("engine")) {
					setEngine(evt.getNewValue());
				} else if (evt.getKey().equals("location")) {
					setLocation(evt.getNewValue());
				} else if (evt.getKey().equals("rendering")) {
					setRendering(evt.getNewValue());
				}
			}
		});
	}

	static ScriptEngineFactory getEngineFactory(String name) {
		ServiceLoader<ScriptEngineFactory> sefLoader = ServiceLoader.load(ScriptEngineFactory.class);
		for (ScriptEngineFactory sef : sefLoader) {
			if(name.equals(sef.getEngineName())) return sef;
		}
		return sefLoader.iterator().next();
	}

	static String getName(String str) {
		return getEngineFactory(str).getNames().get(0);
	}

	public void reset() {
		setEngine(pref.get("engine", ""));
	}

	public String getStylesheet() {
		return stylesheet;
	}

	public void setEngine(String str) {
		ScriptEngineFactory sef = getEngineFactory(str);
		String name = sef.getNames().get(0);
		String init = pref.get(name + ".init", "");
		stylesheet = pref.get(name + ".stylesheet", "");
		engine = sef.getScriptEngine();
		if(init.trim().length() > 0) try {
			engine.eval(init);
		} catch (ScriptException ex) {
			Exceptions.printStackTrace(ex);
		}
	}

	public ScriptEngine getEngine() {
		return engine;
	}

	public void setLocation(String str) {
		try {
			location = new URL(str);
		} catch (MalformedURLException ex) {
			Exceptions.printStackTrace(ex);
		}
	}

	public URL getLocation() {
		return location;
	}

	public void setRendering(String str) {
		rendering = Boolean.valueOf(str);
	}

	public boolean isRendering() {
		return rendering;
	}

	public static MathManager getDefault() {
		return manager;
	}
}
