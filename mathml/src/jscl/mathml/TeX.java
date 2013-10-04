package jscl.mathml;

import java.awt.Image;
import java.io.IOException;
import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.SnuggleEngine;
import uk.ac.ed.ph.snuggletex.SnuggleSession;
import org.w3c.dom.Element;

public class TeX {
	static SnuggleEngine engine;

	static SnuggleEngine engine() {
		return engine==null?engine=new SnuggleEngine():engine;
	}

	static SnuggleSession session(String str) throws IOException {
		SnuggleSession session = engine().createSession();
		session.parseInput(new SnuggleInput(stripMath(str)));
		return session;
	}

	static String mml(String str) throws IOException {
		return session(str).buildXMLString();
	}

	static Image createImage(String str) throws Exception {
		Element e = (Element)session(str).buildDOMSubtree().item(0);
		e.setAttribute("color", "red");
		return MathML.createImage(e);
	}

	static String stripMath(String str) {
		return "$"+str.substring(0,str.length()-7).substring(6)+"$";
	}
}
