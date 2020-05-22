package jscl.editor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.HashMap;
import javax.swing.ImageIcon;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
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
import org.apache.fop.apps.FOPException;
import org.xml.sax.SAXException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

public class MathML extends jscl.converter.Transformer {
	public static final MathML instance = getInstance();
	private final Map<String, Transformer> cache = new HashMap<>();
	private final TransformerFactory factory = TransformerFactory.newInstance();
	private final Image bookmark = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Bookmarks16.gif")).getImage();

	private static MathML getInstance() {
		try {
			return new MathML();
		} catch (final TransformerConfigurationException e) {
			e.printStackTrace();
		}
		return null;
	}

	private Transformer getTransformer(final String stylesheet) throws TransformerConfigurationException {
		if (!cache.containsKey(stylesheet)) {
			cache.put(stylesheet, factory.newTransformer(new StreamSource(getClass().getResource(stylesheet).toString())));
		}
		return cache.get(stylesheet);
	}

	private MathML() throws TransformerConfigurationException {
		super("/net/sourceforge/jeuclid/content/mathmlc2p.xsl");
	}

	public byte[] exportToPDF(final Reader reader, final String stylesheet) throws IOException {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			final FopFactoryBuilder builder = new FopFactoryBuilder(new URI("http://meditorworld.appspot.com/"));
			final FopFactory factory = builder.build();
			JEuclidFopFactoryConfigurator.configure(factory);
			final Fop fop = factory.newFop(MimeConstants.MIME_PDF, out);
			getTransformer(stylesheet).transform(new StreamSource(new StringReader(apply(reader))), new SAXResult(fop.getDefaultHandler()));
		} catch (final URISyntaxException e) {
			e.printStackTrace();
		} catch (final FOPException e) {
			e.printStackTrace();
		} catch (final TransformerException e) {
			e.printStackTrace();
		}
		return out.toByteArray();
	}

	public String exportToXHTML(final Reader reader, final String stylesheet, final String title, final String feed, final String icon) throws IOException {
		return apply(reader, stylesheet, title, feed, icon, null, true);
	}

	@Override
	public String apply(final Reader reader) throws IOException {
		return super.apply(reader).replaceAll("\u2148", "i");
	}

	private String asString(final Color color) {
		return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
	}

	public String fromTeX(final String str) {
		return stripNameSpace(TeX.instance.mml(str));
	}

	public Image createImage(final String str) {
		return createMathImage(XML + insertNameSpace(str), null);
	}

	public Image createImage(final String text, final Color color) {
		final String t = insertNameSpace(text);
		if (isSvg(text)) {
			return SVG.instance.createImage(XML + t);
		} else {
			final Image c = createMathImage(XML + t, asString(color));
			return c == null?TeX.instance.createImage(text):c;
		}
	}

	private Image createMathImage(final String document, final String color) {
		try {
			final Node node = MathMLParserSupport.parseString(transform(new StringReader(document)));
			final Node s = node.getFirstChild();
			final NodeList t = s.getChildNodes();
			if(t.getLength() == 1 && t.item(0).getNodeName().equals("#text")) return null;
			((Element) s).setAttribute("color", color);
			return createImage(node);
		} catch (final SAXException e) {
			e.printStackTrace();
		} catch (final ParserConfigurationException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return bookmark;
	}

	public Image createImage(final Node node) {
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
