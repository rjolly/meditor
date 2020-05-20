package jscl.editor;

import java.io.Reader;
import java.io.Writer;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import jscl.converter.Converter;

public class Code extends Converter {
	private static final Map<String, Code> cache = new HashMap<>();
	private final Transformer transformer;

	private Code(final String stylesheet) throws TransformerConfigurationException {
		transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(getClass().getResource(stylesheet).toString()));
	}

	public static Code instance(final String stylesheet) {
		if (!cache.containsKey(stylesheet)) try {
			cache.put(stylesheet, new Code(stylesheet));
		} catch (final TransformerConfigurationException e) {
			e.printStackTrace();
		}
		return cache.get(stylesheet);
	}

	private String fromString(final String document) {
		final Reader reader = new StringReader(apply(document));
		final Writer writer = new StringWriter();
		try {
			transformer.transform(new StreamSource(reader), new StreamResult(writer));
		} catch (final TransformerException e) {
			e.printStackTrace();
		}
		return writer.toString().replaceAll("\r", "").replaceAll("\u00a0", " ").replaceAll(" +\n", "\n").trim();
	}

	public String apply(final Reader reader) throws IOException {
		return fromString(stringFromReader(reader));
	}
}
