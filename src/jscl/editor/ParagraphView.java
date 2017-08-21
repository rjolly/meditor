package jscl.editor;

import javax.swing.text.Element;

class ParagraphView extends javax.swing.text.ParagraphView {
	public ParagraphView(Element elem) {
		super(elem);
	}

	@Override
	public float nextTabStop(float x, int tabOffset) {
		return getTabBase() + (((int)x / 64 + 1) * 64);
	}
}
