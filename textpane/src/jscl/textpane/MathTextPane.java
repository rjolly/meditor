/*
 * MathTextPane.java
 *
 * Created on December 22, 2006, 3:39 PM
 */

package jscl.textpane;

import java.awt.Dimension;
import java.awt.Image;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JTextPane;
import javax.swing.plaf.basic.BasicEditorPaneUI;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import jscl.converter.Converter;
import jscl.mathml.MathML;
import jscl.mathml.SVG;
import jscl.mathml.TeX;

public class MathTextPane extends JTextPane {
	static final Pattern pattern = Converter.pattern;
	static final String XML = Converter.XML;
	int width;

	public MathTextPane() {
		this.setUI(new BasicEditorPaneUI() {
			public Dimension getMinimumSize(JComponent c) {
				Dimension size=super.getPreferredSize(c);
				return new Dimension(width,size.height);
			}
		});
	}

	public MathDocument getMathDocument() {
		return (MathDocument)getStyledDocument();
	}

	public void setMathDocument(MathDocument doc) {
		width=0;
		setStyledDocument(doc);
	}

	public void replaceSelection(String str) {
		MathDocument doc = getMathDocument();
		doc.hold();
		replaceMathSelection(str);
		doc.release();
	}

	void replaceMathSelection(String str) {
		Matcher pm=pattern.matcher(str);
		int n=0;
		while(pm.find()) {
			int m=pm.start();
			super.replaceSelection(str.substring(n,m));
			String s=pm.group();
			insertIcon(createImageIcon(s));
			n=pm.end();
		}
		super.replaceSelection(str.substring(n));
	}

	static ImageIcon createImageIcon(String text) {
		try {
			return new ImageIcon(createImage(text),text);
		} catch (Exception ex) {
			return new ImageIcon(MathTextPane.class.getResource("/toolbarButtonGraphics/general/Bookmarks16.gif"),text);
		}
	}

	public static Image createImage(String text) throws Exception {
		String t = Converter.insertNameSpace(text);
		if (Converter.isSvg(text)) return SVG.createImage(XML + t);
		else {
			Image c = MathML.createImage(XML + t);
			return c == null?TeX.createImage(text):c;
		}
	}

	public void insertIcon(Icon g) {
		width=Math.max(width,g.getIconWidth());
		AttributeSet a = getInputAttributes().copyAttributes();
		super.insertIcon(g);
		getInputAttributes().addAttributes(a);
	}

	public void unselect() {
		int n=getCaretPosition();
		setSelectionStart(n);
		setSelectionEnd(n);
	}

	@Override
	public String getText() {
		MathDocument doc = getMathDocument();
		try {
			return doc.getText(false);
		} catch (BadLocationException e) {
			return null;
		}
	}

	public boolean findNext(String dstData, boolean wrap) {
		MathDocument doc = getMathDocument();
		String t=getText();
		boolean b=dstData.equals(" ");
		int n=t.indexOf(dstData,getSelectionEnd());
		if(b) while(n!=-1 && doc.getIcon(n)!=null) n=t.indexOf(dstData,n+1);
		if(wrap?n==-1:false) n=t.indexOf(dstData);
		if(b) while(n!=-1 && doc.getIcon(n)!=null) n=t.indexOf(dstData,n+1);
		if(n==-1) return false;
		select(n,n+dstData.length());
		return true;
	}

	public boolean findFirst(String dstData) {
		MathDocument doc = getMathDocument();
		String t=getText();
		boolean b=dstData.equals(" ");
		int n=t.indexOf(dstData);
		if(b) while(n!=-1 && doc.getIcon(n)!=null) n=t.indexOf(dstData,n+1);
		if(n==-1) return false;
		select(n,n+dstData.length());
		return true;
	}

	public void replaceAll(String dstData, String str) {
		MathDocument doc = getMathDocument();
		doc.hold();
		boolean found=findFirst(dstData);
		while(found) {
			replaceMathSelection(str);
			found=findNext(dstData,false);
		}
		doc.release();
	}

	public boolean replace(String dstData, String str) {
		MathDocument doc = getMathDocument();
		doc.hold();
		replaceMathSelection(str);
		doc.release();
		return findNext(dstData,true);
	}
}
