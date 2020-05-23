package jscl.editor;

import java.io.IOException;
import java.io.Reader;
import jscl.converter.Converter;

public class Exporter extends Converter {
	public static final Exporter instance = new Exporter();

	private Exporter() {
	}

	public String exportToXHTML(final Reader reader, final String stylesheet, final String title, final String feed, final String icon) throws IOException {
		return apply(reader, stylesheet, title, feed, icon, null, true);
	}
}
