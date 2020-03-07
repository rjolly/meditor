package jscl.editor;

import java.awt.Image;
import java.awt.Component;
import java.io.Writer;
import java.io.StringReader;
import java.io.StringWriter;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;

public class SVG {
	public static final SVG instance = new SVG();

	private SVG() {
	}

	public String print(final Component comp) throws Exception {
		final SVGGraphics2D g = new SVGGraphics2D(SVGDOMImplementation.getDOMImplementation().createDocument(SVGDOMImplementation.SVG_NAMESPACE_URI, "svg", null));
		g.setSVGCanvasSize(comp.getSize());
		comp.print(g);

		final Writer writer = new StringWriter();
		g.stream(writer);

		String s = writer.toString();
		s = s.replaceAll("\r", "");
		for (int i = 0 ; i < 3 ; i++) s = s.substring(s.indexOf('\n') + 1);
		return s.substring(0, s.lastIndexOf('\n'));
	}

	public Image createImage(final String document) throws Exception {
		final MemoryTranscoder transcoder = new MemoryTranscoder();
		transcoder.transcode(new TranscoderInput(new StringReader(document)), new TranscoderOutput());
		return transcoder.getImage();
	}
}
