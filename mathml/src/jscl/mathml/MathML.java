package jscl.mathml;

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
	public static byte[] exportToPDF(String document, String stylesheet) throws Exception {
		String str = c2p(Converter.apply(document, stylesheet));
		Reader r=new StringReader(str);
		ByteArrayOutputStream out=new ByteArrayOutputStream();
		Fop fop = FopFactory.newInstance().newFop(MimeConstants.MIME_PDF, out);
		Converter.instance(stylesheet).transformer.transform(new StreamSource(r), new SAXResult(fop.getDefaultHandler()));
		return out.toByteArray();
	}

	public static String exportToXHTML(String document, String stylesheet, String title, String feed, String icon) {
		return Converter.apply(document, stylesheet, title, feed, icon, null, true);
	}

	public static String code(String document, String stylesheet) throws Exception {
		return Converter.instance(stylesheet).apply(document);
	}

	static String c2p(String document) throws Exception {
		Reader r=new StringReader(document);
		Writer w=new StringWriter();
		Converter.instance("/jscl/mathml/mathmlc2p.xsl").transformer.transform(new StreamSource(r), new StreamResult(w));
		return w.toString().replaceAll("\u2148", "i");
	}

	public static Image createImage(String text) throws Exception {
		String t = Converter.insertNameSpace(text);
		if (Converter.isSvg(text)) return SVG.createImage(Converter.XML + t);
		else {
			Image c = createMathImage(Converter.XML + t);
			return c == null?TeX.createImage(text):c;
		}
	}

	static Image createMathImage(String document) throws Exception {
		String str = c2p(document);
		Node node = MathMLParserSupport.parseString(str);
		NodeList t=node.getFirstChild().getChildNodes();
		if(t.getLength()==1 && t.item(0).getNodeName().equals("#text")) return null;
		return createImage(node);
	}

	static Image createImage(Node node) throws Exception {
		((MutableLayoutContext)LayoutContextImpl.getDefaultLayoutContext()).setParameter(Parameter.SCRIPTMINSIZE, new Float(10f));
		JEuclidView view=DOMBuilder.getInstance().createJeuclidDom(node).getDefaultView();
		int width = (int)Math.ceil(view.getWidth());
		int height = (int)(Math.ceil(view.getAscentHeight())+Math.ceil(view.getDescentHeight()));

		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

		Color transparency = new Color(255, 255, 255, 0);

		g.setColor(transparency);
		g.fillRect(0, 0, width, height);
		g.setColor(Color.black);

		view.draw(g, 0, (float)Math.ceil(view.getAscentHeight()));
		return image;
	}
}
