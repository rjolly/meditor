package jscl.editor;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.HashMap;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import net.sourceforge.jeuclid.fop.plugin.JEuclidFopFactoryConfigurator;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.apps.FOPException;

public class PDF extends Content {
	private static final Map<String, PDF> cache = new HashMap<>();
	private final Transformer transformer;

	private PDF(final String stylesheet) throws TransformerConfigurationException {
		transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(getClass().getResource(stylesheet).toString()));
	}

	public static PDF instance(final String stylesheet) {
		if (!cache.containsKey(stylesheet)) try {
			cache.put(stylesheet, new PDF(stylesheet));
		} catch (final TransformerConfigurationException e) {
			e.printStackTrace();
		}
		return cache.get(stylesheet);
	}

	public byte[] exportToPDF(final Reader reader) throws IOException {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			final FopFactoryBuilder builder = new FopFactoryBuilder(new URI("http://meditorworld.appspot.com/"));
			final FopFactory factory = builder.build();
			JEuclidFopFactoryConfigurator.configure(factory);
			final Fop fop = factory.newFop(MimeConstants.MIME_PDF, out);
			transformer.transform(new StreamSource(new StringReader(apply(reader))), new SAXResult(fop.getDefaultHandler()));
		} catch (final URISyntaxException e) {
			e.printStackTrace();
		} catch (final FOPException e) {
			e.printStackTrace();
		} catch (final TransformerException e) {
			e.printStackTrace();
		}
		return out.toByteArray();
	}
}
