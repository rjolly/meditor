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
	private String name;
	private ScriptEngine engine;
	private String engineStylesheet;
	private boolean rendering;
	private URL location;
	private String stylesheet;
	private String feed;
	private String icon;

	public MathManager() {
		setEngine(pref.get("engine", ""));
		setLocation(pref.get("location", ""));
		setStylesheet(pref.get("stylesheet", ""));
		setFeed(pref.get("feed", ""));
		setIcon(pref.get("icon", ""));

		pref.addPreferenceChangeListener(new PreferenceChangeListener() {
			public void preferenceChange(PreferenceChangeEvent evt) {
				if (evt.getKey().equals("engine")) {
					setEngine(evt.getNewValue());
				} else if (evt.getKey().equals("location")) {
					setLocation(evt.getNewValue());
				} else if (evt.getKey().equals("stylesheet")) {
					setStylesheet(evt.getNewValue());
				} else if (evt.getKey().equals("feed")) {
					setFeed(evt.getNewValue());
				} else if (evt.getKey().equals("icon")) {
					setIcon(evt.getNewValue());
				}
			}
		});
	}

	public static ScriptEngineFactory getEngineFactory(String name) {
		ServiceLoader<ScriptEngineFactory> sefLoader = ServiceLoader.load(ScriptEngineFactory.class);
		for (ScriptEngineFactory sef : sefLoader) {
			if(name.equals(sef.getEngineName())) return sef;
		}
		return sefLoader.iterator().next();
	}

	public void reset() {
		setEngine(pref.get("engine", ""));
	}

	public boolean isRendering() {
		return pref.getBoolean(name + ".rendering", false);
	}

	public String getEngineStylesheet() {
		return pref.get(name + ".stylesheet", "");
	}

	public void setEngine(String str) {
		final ScriptEngineFactory sef = getEngineFactory(str);
		name = sef.getNames().get(0);
		engine = sef.getScriptEngine();
		final String init = pref.get(name + ".init", "");
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

	public void setStylesheet(String str) {
		stylesheet = str;
	}

	public String getStylesheet() {
		return stylesheet;
	}

	public void setFeed(String str) {
		feed = str;
	}

	public String getFeed() {
		return feed;
	}

	public void setIcon(String str) {
		icon = str;
	}

	public String getIcon() {
		return icon;
	}

	public static MathManager getDefault() {
		return manager;
	}
}
