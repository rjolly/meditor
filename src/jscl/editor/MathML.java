package jscl.editor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import javax.swing.ImageIcon;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import net.sourceforge.jeuclid.DOMBuilder;
import net.sourceforge.jeuclid.MathMLParserSupport;
import net.sourceforge.jeuclid.MutableLayoutContext;
import net.sourceforge.jeuclid.context.LayoutContextImpl;
import net.sourceforge.jeuclid.context.Parameter;
import net.sourceforge.jeuclid.layout.JEuclidView;
import org.xml.sax.SAXException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

public class MathML extends Content {
	public static final MathML instance = getInstance();
	private final Image bookmark = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Bookmarks16.gif")).getImage();

	private static MathML getInstance() {
		try {
			return new MathML();
		} catch (final TransformerConfigurationException e) {
			e.printStackTrace();
		}
		return null;
	}

	private MathML() throws TransformerConfigurationException {
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
			final Node node = MathMLParserSupport.parseString(c2p(document));
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
