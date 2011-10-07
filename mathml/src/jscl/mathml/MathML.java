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
import java.util.Map;
import java.util.HashMap;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
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
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

public class MathML {
	static final Map<String, Transformer> cache = new HashMap<String, Transformer>();

	public static String code(String document, String stylesheet) throws Exception {
		return transform(Converter.convert(document), stylesheet).replaceAll("\u00a0"," ");
	}

	public static byte[] exportToPDF(String document) throws Exception {
		String str = c2p(Converter.convert(document));
		Reader r=new StringReader(str);
		ByteArrayOutputStream out=new ByteArrayOutputStream();
		Fop fop = FopFactory.newInstance().newFop(MimeConstants.MIME_PDF, out);
		transformer("/jscl/mathml/xhtmlfo.xsl").transform(new StreamSource(r), new SAXResult(fop.getDefaultHandler()));
		return out.toByteArray();
	}

	static String tex(String document) throws TransformerException {
		return transform(Converter.convert(document), "/jscl/mathml/xsltml/mmltex.xsl").replaceAll("\u00a0"," ");
	}

	static String c2p(String document) throws TransformerException {
		return transform(document, "/jscl/mathml/mathmlc2p.xsl");
	}

	static String transform(String document, String stylesheet) throws TransformerException {
		Reader r=new StringReader(document);
		Writer w=new StringWriter();
		transformer(stylesheet).transform(new StreamSource(r), new StreamResult(w));
		return w.toString();
	}

	static Transformer transformer(String stylesheet) throws TransformerException {
		if(!cache.containsKey(stylesheet)) cache.put(stylesheet,TransformerFactory.newInstance().newTransformer(new StreamSource(MathML.class.getResource(stylesheet).toString())));
		return cache.get(stylesheet);
	}

	public static Image createImage(String document) throws Exception {
		Node node = MathMLParserSupport.parseString(c2p(document));
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
