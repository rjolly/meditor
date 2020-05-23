package jscl.converter;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class Transformer extends Converter {
	private final javax.xml.transform.Transformer transformer;

	public Transformer(final String stylesheet) throws TransformerConfigurationException {
		transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(getClass().getResource(stylesheet).toString()));
	}

	public String apply(final Reader reader) throws IOException {
		return transform(new StringReader(apply(reader, null, null, null, null, null, false)));
	}

	protected String transform(final Reader reader) {
		final Writer writer = new StringWriter();
		try {
			transformer.transform(new StreamSource(reader), new StreamResult(writer));
		} catch (final TransformerException e) {
			e.printStackTrace();
		}
		return writer.toString();
	}

	public void transform(final Reader reader, final Writer writer) throws IOException {
		pipe(new StringReader(apply(reader)), writer);
	}
}
