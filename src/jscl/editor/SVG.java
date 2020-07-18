package jscl.editor;

import java.awt.Image;
import java.awt.Component;
import java.awt.Dimension;
import java.io.Writer;
import java.io.StringReader;
import java.io.StringWriter;
import javax.swing.ImageIcon;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscoderException;

public class SVG {
	public static final SVG instance = new SVG();
	private final Image bookmark = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Bookmarks16.gif")).getImage();

	private SVG() {
	}

	public String print(final Component comp) {
		final SVGGraphics2D g = new SVGGraphics2D(SVGDOMImplementation.getDOMImplementation().createDocument(SVGDOMImplementation.SVG_NAMESPACE_URI, "svg", null));
		final Dimension size = comp.getSize();
		g.setSVGCanvasSize(size);
		comp.print(g);
		final Writer writer = new StringWriter();
		try {
			g.stream(writer);
		} catch (final SVGGraphics2DIOException e) {
			e.printStackTrace();
		}
		String s = writer.toString();
		s = s.replaceAll(" NaN", " " + size.getHeight()).replaceAll("\r", "");
		for (int i = 0 ; i < 3 ; i++) s = s.substring(s.indexOf('\n') + 1);
		return s.substring(0, s.lastIndexOf('\n'));
	}

	public Image createImage(final String document) {
		final MemoryTranscoder transcoder = new MemoryTranscoder();
		try {
			transcoder.transcode(new TranscoderInput(new StringReader(document)), new TranscoderOutput());
			return transcoder.getImage();
		} catch (final TranscoderException e) {
			e.printStackTrace();
		}
		return bookmark;
	}
}
