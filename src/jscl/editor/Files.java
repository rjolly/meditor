package jscl.editor;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;

public class Files {
	public static final Files instance = new Files();
	private final Code code = Code.instance("listfiles.xsl");

	private Files() {
	}

	public List<URL> list(final String url) throws IOException {
		final URL base = new URL(url);
		final URL index = new URL(base, "index.txt");
		final String files[] = code.apply(new InputStreamReader(index.openStream())).split("\n");
		final List<URL> list = new ArrayList<>();
		list.add(index);
		for (final String file : files) {
			list.add(new URL(base, file));
		}
		return list;
	}
}
