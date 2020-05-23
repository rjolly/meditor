package jscl.editor;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import javax.xml.transform.TransformerConfigurationException;
import jscl.converter.Transformer;

public class Content extends Transformer {
	protected Content() throws TransformerConfigurationException {
		super("/net/sourceforge/jeuclid/content/mathmlc2p.xsl");
	}

	@Override
	public String apply(final Reader reader) throws IOException {
		return super.apply(reader).replaceAll("\u2148", "i");
	}

	protected String c2p(final String document) {
		return transform(new StringReader(document));
	}
}
