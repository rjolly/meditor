/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jscl.editor;

import java.awt.Font;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import javax.swing.JEditorPane;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import jscl.textpane.MathDocument;
import jscl.textpane.MathTextPane;
import org.openide.util.Exceptions;

/**
 *
 * @author raphael
 */
public class MathEditorKit extends StyledEditorKit {
	private JEditorPane editor;

	@Override
	public Document createDefaultDocument() {
		return new MathDocument();
	}

	@Override
	public void read(Reader in, Document doc, int pos) throws IOException, BadLocationException {
		String str = read(in);
		boolean edit = editor.isEditable();
		editor.setEditable(true);
		editor.replaceSelection(str);
		editor.setCaretPosition(0);
		editor.setEditable(edit);
	}

	String read(Reader in) {
		StringBuffer buffer = new StringBuffer();
		try {
			int n;
			char data[] = new char[4096];
			while ((n = in.read(data)) > 0) {
				buffer.append(data, 0, n);
			}
		} catch (IOException ex) {
			Exceptions.printStackTrace(ex);
		}
		return buffer.toString();
	}

	@Override
	public void write(Writer out, Document doc, int pos, int len) throws IOException, BadLocationException {
		String text = ((MathTextPane)editor).getMathDocument().getText();
		out.write(text);
	}

	public void install(JEditorPane c) {
		Font font = c.getFont();
		MutableAttributeSet attr = new SimpleAttributeSet();
		StyleConstants.setFontSize(attr, font.getSize());
		StyleConstants.setFontFamily(attr, font.getFamily());
		StyleConstants.setBold(attr, font.isBold());
		StyleConstants.setItalic(attr, font.isItalic());
		getInputAttributes().addAttributes(attr);
		c.getInputMap().put(KeyStroke.getKeyStroke("ctrl H"), "none");
		editor = c;
	}
}
