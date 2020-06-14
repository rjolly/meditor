package jscl.editor;

import java.io.Reader;
import java.io.StringReader;
import javax.xml.transform.TransformerConfigurationException;
import jscl.converter.Transformer;

public class Content extends Transformer {
	protected Content() throws TransformerConfigurationException {
		super("/net/sourceforge/jeuclid/content/mathmlc2p.xsl");
	}

	@Override
	protected String transform(final Reader reader) {
		return super.transform(reader).replaceAll("\u2148", "i");
	}

	protected String c2p(final String document) {
		return transform(new StringReader(document));
	}
}
