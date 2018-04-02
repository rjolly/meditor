package jscl.editor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import javax.swing.JEditorPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.ViewFactory;

public class MathEditorKit extends StyledEditorKit {
	private MathTextPane editor;
	private FileWorker worker;

	@Override
	public MathDocument createDefaultDocument() {
		return new MathDocument();
	}

	@Override
	public ViewFactory getViewFactory() {
		return new MathViewFactory();
	}

	public void setWorker(final FileWorker worker) {
		this.worker = worker;
	}

	@Override
	public void read(Reader in, Document doc, int pos) throws IOException {
		final StringBuffer buffer = new StringBuffer();
		final char[] data = new char[4096];
		int n;
		int s = 0;
		while ((n = in.read(data)) > 0) {
			buffer.append(data, 0, n);
			worker.setNumber(s += n);
		}
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					editor.replaceSelection(buffer.toString());
					editor.setCaretPosition(0);
				}
			});
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void write(Writer out, Document doc, int pos, int len) throws IOException, BadLocationException {
		final String text = editor.getDocument().getText();
		int charsLeft = text.length();
		int offset = 0;
		while (charsLeft > 0) {
			final int n = Math.min(4096, charsLeft);
			out.write(text.substring(offset, offset + n));
			charsLeft -= n;
			offset += n;
			worker.setNumber(offset);
		}
	}

	@Override
	public void install(JEditorPane c) {
		final MutableAttributeSet attr = new SimpleAttributeSet();
		StyleConstants.setFontFamily(attr, "Monospaced");
		getInputAttributes().addAttributes(attr);
		c.getInputMap().put(KeyStroke.getKeyStroke("ctrl H"), "none");
		editor = (MathTextPane) c;
	}
}
