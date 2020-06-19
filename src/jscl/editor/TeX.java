package jscl.editor;

import java.io.IOException;
import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.SnuggleEngine;
import uk.ac.ed.ph.snuggletex.SnuggleSession;
import org.w3c.dom.Node;

public class TeX {
	public static final TeX instance = new TeX();
	private SnuggleEngine engine = new SnuggleEngine();

	private TeX() {
	}

	private SnuggleSession session(final String str) {
		final SnuggleSession session = engine.createSession();
		try {
			session.parseInput(new SnuggleInput("$" + str + "$"));
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return session;
	}

	public String mml(final String str) {
		return session(stripMath(str)).buildXMLString();
	}

	public Node toMathML(final String str) {
		return session(str).buildDOMSubtree().item(0);
	}

	private String stripMath(final String str) {
		return str.substring(0, str.length() - 7).substring(6);
	}
}
