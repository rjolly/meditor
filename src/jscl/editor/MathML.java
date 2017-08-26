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
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

public class MathML {
	public static final MathML instance = new MathML();

	private MathML() {
	}

	public byte[] exportToPDF(final String document, final String stylesheet) throws Exception {
		final String str = c2p(Converter.apply(document, null));
		final Reader r = new StringReader(str);
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final Fop fop = FopFactory.newInstance().newFop(MimeConstants.MIME_PDF, out);
		Converter.instance(stylesheet).transformer.transform(new StreamSource(r), new SAXResult(fop.getDefaultHandler()));
		return out.toByteArray();
	}

	public String exportToXHTML(final String document, final String stylesheet, final String title, final String feed, final String icon) {
		return Converter.apply(document, stylesheet, title, feed, icon, null, true);
	}

	public String code(final String document, final String stylesheet) throws Exception {
		return Converter.instance(stylesheet).apply(document);
	}

	private String c2p(final String document) throws Exception {
		final Reader r = new StringReader(document);
		final Writer w = new StringWriter();
		Converter.instance("/net/sourceforge/jeuclid/content/mathmlc2p.xsl").transformer.transform(new StreamSource(r), new StreamResult(w));
		return w.toString().replaceAll("\u2148", "i");
	}

	public Image createImage(final String text) throws Exception {
		final String t = Converter.insertNameSpace(text);
		if (Converter.isSvg(text)) {
			return SVG.instance.createImage(Converter.XML + t);
		} else {
			return createMathImage(Converter.XML + t);
		}
	}

	private Image createMathImage(final String document) throws Exception {
		final String str = c2p(document);
		final Node node = MathMLParserSupport.parseString(str);
		final NodeList t = node.getFirstChild().getChildNodes();
		if(t.getLength() == 1 && t.item(0).getNodeName().equals("#text")) return null;
		return createImage(node);
	}

	private Image createImage(final Node node) throws Exception {
		((MutableLayoutContext)LayoutContextImpl.getDefaultLayoutContext()).setParameter(Parameter.SCRIPTMINSIZE, new Float(10f));
		final JEuclidView view=DOMBuilder.getInstance().createJeuclidDom(node).getDefaultView();
		final int width = (int) Math.ceil(view.getWidth());
		final int height = (int) (Math.ceil(view.getAscentHeight()) + Math.ceil(view.getDescentHeight()));

		final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g = image.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

		final Color transparency = new Color(255, 255, 255, 0);

		g.setColor(transparency);
		g.fillRect(0, 0, width, height);
		g.setColor(Color.black);

		view.draw(g, 0, (float)Math.ceil(view.getAscentHeight()));
		return image;
	}
}
