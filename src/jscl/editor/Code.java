package jscl.editor;

import java.io.Reader;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import jscl.converter.Transformer;
import javax.xml.transform.TransformerConfigurationException;

public class Code extends Transformer {
	private static final Map<String, Code> cache = new HashMap<>();

	private Code(final String stylesheet) throws TransformerConfigurationException {
		super(stylesheet);
	}

	public static Code instance(final String stylesheet) {
		if (!cache.containsKey(stylesheet)) try {
			cache.put(stylesheet, new Code(stylesheet));
		} catch (final TransformerConfigurationException e) {
			e.printStackTrace();
		}
		return cache.get(stylesheet);
	}

	@Override
	public String apply(final Reader reader) throws IOException {
		return trim(super.apply(reader).replaceAll("\r", "").replaceAll("\u00a0", " ").replaceAll(" +\n", "\n"));
	}

	private String trim(String str) {
		return str.substring(5, str.length() - 2);
	}
}
