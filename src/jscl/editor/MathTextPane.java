package jscl.editor;

import java.util.regex.Matcher;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.Element;
import jscl.converter.Converter;

public class MathTextPane extends JTextPane {
	@Override
	protected EditorKit createDefaultEditorKit() {
		return new MathEditorKit();
	}

	@Override
	public MathEditorKit getEditorKit() {
		return (MathEditorKit) super.getEditorKit();
	}

	@Override
	public MathDocument getDocument() {
		return (MathDocument) super.getDocument();
	}

	@Override
	public void setDocument(final Document doc) {
		if (doc instanceof MathDocument) {
			setMathDocument((MathDocument) doc);
		} else {
			throw new IllegalArgumentException("Model must be MathDocument");
		}
	}

	public void setMathDocument(final MathDocument doc) {
		setStyledDocument(doc);
	}

	public int getLineOfOffset(final int offset) throws BadLocationException {
		final Document doc = getDocument();
		if (offset < 0) {
			throw new BadLocationException("Can't translate offset to line", -1);
		} else if (offset > doc.getLength()) {
			throw new BadLocationException("Can't translate offset to line", doc.getLength()+1);
		} else {
			Element map = getDocument().getDefaultRootElement();
			return map.getElementIndex(offset);
		}
	}

	public int getLineCount() {
		final Element map = getDocument().getDefaultRootElement();
		return map.getElementCount();
	}

	public int getLineStartOffset(final int line) throws BadLocationException {
		final int lineCount = getLineCount();
		if (line < 0) {
			throw new BadLocationException("Negative line", -1);
		} else if (line >= lineCount) {
			throw new BadLocationException("No such line", getDocument().getLength()+1);
		} else {
			final Element map = getDocument().getDefaultRootElement();
			final Element lineElem = map.getElement(line);
			return lineElem.getStartOffset();
		}
	}

	public int getLineEndOffset(final int line) throws BadLocationException {
		final int lineCount = getLineCount();
		if (line < 0) {
			throw new BadLocationException("Negative line", -1);
		} else if (line >= lineCount) {
			throw new BadLocationException("No such line", getDocument().getLength()+1);
		} else {
			final Element map = getDocument().getDefaultRootElement();
			final Element lineElem = map.getElement(line);
			final int endOffset = lineElem.getEndOffset();
			// hide the implicit break at the end of the document
			return ((line == lineCount - 1) ? (endOffset - 1) : endOffset);
		}
	}

	@Override
	public void replaceSelection(final String str) {
		final MathDocument doc = getDocument();
		doc.hold();
		replaceMathSelection(str);
		doc.release();
	}

	void replaceMathSelection(final String str) {
		final Matcher pm = Converter.pattern.matcher(str);
		int n=0;
		while(pm.find()) {
			final int m = pm.start();
			super.replaceSelection(str.substring(n, m));
			final String s = pm.group();
			insertIcon(createImageIcon(s));
			n = pm.end();
		}
		super.replaceSelection(str.substring(n));
	}

	private ImageIcon createImageIcon(final String text) {
		try {
			return new ImageIcon(MathML.instance.createImage(text, getForeground()), text);
		} catch (final Exception ex) {
			return new ImageIcon(MathTextPane.class.getResource("/toolbarButtonGraphics/general/Bookmarks16.gif"), text);
		}
	}

	public void insertIcon(final Icon g) {
		final AttributeSet a = getInputAttributes().copyAttributes();
		super.insertIcon(g);
		getInputAttributes().addAttributes(a);
	}

	public void unselect() {
		final int n = getCaretPosition();
		setSelectionStart(n);
		setSelectionEnd(n);
	}

	@Override
	public String getText() {
		return getText(true);
	}

	public String getText(final boolean expand) {
		final MathDocument doc = getDocument();
		try {
			return doc.getText(expand);
		} catch (final BadLocationException e) {
			return null;
		}
	}

	public boolean findNext(final String dstData, final boolean wrap) {
		final MathDocument doc = getDocument();
		final String t = getText(false);
		final boolean b = dstData.equals(" ");
		int n = t.indexOf(dstData, getSelectionEnd());
		if(b) while (n != -1 && doc.getIcon(n) != null) n = t.indexOf(dstData, n + 1);
		if(wrap?n == -1:false) n = t.indexOf(dstData);
		if(b) while (n != -1 && doc.getIcon(n) != null) n = t.indexOf(dstData, n + 1);
		if(n == -1) return false;
		select(n, n + dstData.length());
		return true;
	}

	public boolean findFirst(final String dstData) {
		final MathDocument doc = getDocument();
		final String t = getText(false);
		final boolean b = dstData.equals(" ");
		int n = t.indexOf(dstData);
		if(b) while (n != -1 && doc.getIcon(n) != null) n = t.indexOf(dstData, n + 1);
		if(n == -1) return false;
		select(n, n + dstData.length());
		return true;
	}

	public void replaceAll(final String dstData, final String str) {
		final MathDocument doc = getDocument();
		doc.hold();
		boolean found = findFirst(dstData);
		while (found) {
			replaceMathSelection(str);
			found = findNext(dstData, false);
		}
		doc.release();
	}

	public boolean replace(final String dstData, final String str) {
		final MathDocument doc = getDocument();
		doc.hold();
		replaceMathSelection(str);
		doc.release();
		return findNext(dstData,true);
	}
}
