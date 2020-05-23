package jscl.editor;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.io.FileWriter;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import javax.xml.transform.TransformerConfigurationException;
import jscl.converter.Transformer;

public class Files extends Transformer {
	public static final Files instance = getInstance();

	private static Files getInstance() {
		try {
			return new Files();
		} catch (final TransformerConfigurationException e) {
			e.printStackTrace();
		}
		return null;
	}

	private Files() throws TransformerConfigurationException {
		super("listfiles.xsl");
	}

	public List<URL> list(final String url) throws IOException {
		final URL base = new URL(url);
		final URL index = new URL(base, "index.txt");
		final String files[] = apply(new InputStreamReader(index.openStream())).trim().split("\n");
		final List<URL> list = new ArrayList<>();
		list.add(index);
		for (final String file : files) {
			list.add(new URL(base, file));
		}
		return list;
	}

	public void dump(final String url, final File dir, final String stylesheet) throws IOException {
		final Code code = Code.instance(stylesheet);
		for (final URL file : list(url)) {
			final String name = new File(file.getFile()).getName();
			System.out.println(name);
			try (final Reader reader = new InputStreamReader(file.openStream()); final Writer writer = new FileWriter(new File(dir, name))) {
				code.transform(reader, writer);
				writer.write("\n");
			} catch (final FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
}
