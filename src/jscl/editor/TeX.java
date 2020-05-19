package jscl.editor;

import java.awt.Image;
import java.io.IOException;
import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.SnuggleEngine;
import uk.ac.ed.ph.snuggletex.SnuggleSession;
import org.w3c.dom.Element;

public class TeX {
	public static final TeX instance = new TeX();
	private SnuggleEngine engine = new SnuggleEngine();

	private TeX() {
	}

	private SnuggleSession session(final String str) throws IOException {
		final SnuggleSession session = engine.createSession();
		session.parseInput(new SnuggleInput(stripMath(str)));
		return session;
	}

	public String mml(final String str) throws IOException {
		return session(str).buildXMLString();
	}

	public Image createImage(final String str) throws IOException {
		final Element e = (Element) session(str).buildDOMSubtree().item(0);
		e.setAttribute("color", "red");
		return MathML.instance.createImage(e);
	}

	private String stripMath(final String str) {
		return "$" + str.substring(0, str.length() - 7).substring(6) + "$";
	}
}
