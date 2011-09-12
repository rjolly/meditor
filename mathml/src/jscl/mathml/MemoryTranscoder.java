package jscl.mathml;

import java.awt.Image;
import java.awt.image.BufferedImage;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;

public class MemoryTranscoder extends ImageTranscoder {
	Image image;

	public BufferedImage createImage(int width, int height) {
		return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	}

	public void writeImage(BufferedImage img, TranscoderOutput output) {
		image=img;
	}

	public Image getImage() {
		return image;
	}
}
