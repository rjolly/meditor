package jscl.mathml;

import java.awt.Image;
import java.awt.Component;
import java.io.Writer;
import java.io.StringReader;
import java.io.StringWriter;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;

public class SVG {
	public static String print(Component comp) throws Exception {
		SVGGraphics2D g = new SVGGraphics2D(SVGDOMImplementation.getDOMImplementation().createDocument(SVGDOMImplementation.SVG_NAMESPACE_URI, "svg", null));
		g.setSVGCanvasSize(comp.getSize());
		comp.print(g);

		Writer w=new StringWriter();
		g.stream(w);

		String s = w.toString();
		for(int i=0;i<3;i++) s = s.substring(s.indexOf('\n')+1);
		return s.substring(0, s.lastIndexOf('\n'));
	}

	public static Image createImage(String document) throws Exception {
		MemoryTranscoder t=new MemoryTranscoder();
		t.transcode(new TranscoderInput(new StringReader(document)), new TranscoderOutput());
		return t.getImage();
	}
}
