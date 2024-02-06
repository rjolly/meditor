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

	public Image createImage(final String str, final Color color) {
		return isSvg(str)?SVG.instance.createImage(XML + insertNameSpace(str)):createMathImage(XML + insertNameSpace(str), asString(color));
	}

	private Image createMathImage(final String document, final String color) {
		try {
			final Node node = MathMLParserSupport.parseString(c2p(document));
			final Node s = node.getFirstChild();
			final NodeList t = s.getChildNodes();
			if(t.getLength() == 1 && t.item(0).getNodeName().equals("#text")) {
				final Element e = (Element) TeX.instance.toMathML(t.item(0).getNodeValue());
				e.setAttribute("color", "red");
				return createImage(e);
			} else {
				final Element e = (Element) s;
				e.setAttribute("color", color);
				return createImage(e);
			}
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
		((MutableLayoutContext) LayoutContextImpl.getDefaultLayoutContext()).setParameter(Parameter.SCRIPTMINSIZE, 10f);
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
