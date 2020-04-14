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
	private final TransformerFactory factory = TransformerFactory.newInstance("com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl", null);

	private Code(final String stylesheet) throws TransformerConfigurationException {
		transformer = factory.newTransformer(new StreamSource(getClass().getResource(stylesheet).toString()));
	}

	public static Code instance(final String stylesheet) throws TransformerConfigurationException {
		if (!cache.containsKey(stylesheet)) {
			cache.put(stylesheet, new Code(stylesheet));
		}
		return cache.get(stylesheet);
	}

	private String fromString(final String document) throws TransformerException {
		final Reader reader = new StringReader(apply(document));
		final Writer writer = new StringWriter();
		transformer.transform(new StreamSource(reader), new StreamResult(writer));
		return writer.toString().replaceAll("\r", "").replaceAll("\u00a0", " ").replaceAll(" +\n", "\n").trim();
	}

	public String apply(final Reader reader) throws TransformerException, IOException {
		return fromString(stringFromReader(reader));
	}
}
