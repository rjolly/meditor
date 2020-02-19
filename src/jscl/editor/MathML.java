package jscl.editor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.Reader;
import java.io.Writer;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.Map;
import java.util.HashMap;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import jscl.converter.Converter;
import net.sourceforge.jeuclid.DOMBuilder;
import net.sourceforge.jeuclid.MathMLParserSupport;
import net.sourceforge.jeuclid.MutableLayoutContext;
import net.sourceforge.jeuclid.context.LayoutContextImpl;
import net.sourceforge.jeuclid.context.Parameter;
import net.sourceforge.jeuclid.layout.JEuclidView;
import net.sourceforge.jeuclid.fop.plugin.JEuclidFopFactoryConfigurator;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.fop.apps.MimeConstants;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

public class MathML {
	public static final MathML instance = new MathML();
	private final Map<String, Transformer> cache = new HashMap<>();
	private final TransformerFactory factory = TransformerFactory.newInstance();

	private Transformer getTransformer(final String stylesheet) throws TransformerConfigurationException {
		if (!cache.containsKey(stylesheet)) {
			cache.put(stylesheet, factory.newTransformer(new StreamSource(getClass().getResource(stylesheet).toString())));
		}
 		return cache.get(stylesheet);
	}

	private MathML() {
	}

	public byte[] exportToPDF(final String document, final String stylesheet) throws Exception {
		final String str = c2p(Converter.apply(document, null));
		final Reader r = new StringReader(str);
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final FopFactoryBuilder builder = new FopFactoryBuilder(new URI("http://meditorworld.appspot.com/"));
		final FopFactory factory = builder.build();
		JEuclidFopFactoryConfigurator.configure(factory);
		final Fop fop = factory.newFop(MimeConstants.MIME_PDF, out);
		getTransformer(stylesheet).transform(new StreamSource(r), new SAXResult(fop.getDefaultHandler()));
		return out.toByteArray();
	}

	public String exportToXHTML(final String document, final String stylesheet, final String title, final String feed, final String icon) {
		return Converter.apply(document, stylesheet, title, feed, icon, null, true);
	}

	public String code(final String document, final String stylesheet) throws Exception {
		final String s = Converter.apply(document, null);
		final Reader r = new StringReader(s);
		final Writer w = new StringWriter();
		getTransformer(stylesheet).transform(new StreamSource(r), new StreamResult(w));
		return Converter.replace(w.toString());
	}

	private String c2p(final String document) throws Exception {
		final Reader r = new StringReader(document);
		final Writer w = new StringWriter();
		getTransformer("/net/sourceforge/jeuclid/content/mathmlc2p.xsl").transform(new StreamSource(r), new StreamResult(w));
		return w.toString().replaceAll("\u2148", "i");
	}

	private String asString(final Color color) {
		return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
	}

	public Image createImage(final String text, final Color color) throws Exception {
		final String t = Converter.insertNameSpace(text);
		if (Converter.isSvg(text)) {
			return SVG.instance.createImage(Converter.XML + t);
		} else {
			final Image c = createMathImage(Converter.XML + t, asString(color));
			return c == null?TeX.instance.createImage(text):c;
		}
	}

	Image createMathImage(final String document, final String color) throws Exception {
		final String str = c2p(document);
		final Node node = MathMLParserSupport.parseString(str);
		final Node s = node.getFirstChild();
		final NodeList t = s.getChildNodes();
		if(t.getLength() == 1 && t.item(0).getNodeName().equals("#text")) return null;
		((Element) s).setAttribute("color", color);
		return createImage(node);
	}

	Image createImage(final Node node) throws Exception {
		((MutableLayoutContext) LayoutContextImpl.getDefaultLayoutContext()).setParameter(Parameter.SCRIPTMINSIZE, new Float(10f));
		final JEuclidView view = DOMBuilder.getInstance().createJeuclidDom(node).getDefaultView();
		final int width = (int) Math.ceil(view.getWidth());
		final int height = (int) (Math.ceil(view.getAscentHeight()) + Math.ceil(view.getDescentHeight()));

		final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g = image.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

		final Color transparency = new Color(255, 255, 255, 0);

		g.setColor(transparency);
		g.fillRect(0, 0, width, height);
		g.setColor(Color.black);

		view.draw(g, 0, (float) Math.ceil(view.getAscentHeight()));
		return image;
	}
}
