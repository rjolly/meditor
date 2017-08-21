package jscl.editor;

import javax.swing.ImageIcon;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.Element;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyleConstants;
import javax.swing.text.BadLocationException;
import javax.swing.undo.CompoundEdit;

public class MathDocument extends DefaultStyledDocument {
	CompoundEdit undo = null;

	void hold() {
		undo = new CompoundEdit();
	}

	void release() {
		undo.end();
		super.fireUndoableEditUpdate(new UndoableEditEvent(this, undo));
		undo = null;
	}

	protected void fireUndoableEditUpdate(UndoableEditEvent e) {
		if(undo==null) super.fireUndoableEditUpdate(e);
		else undo.addEdit(e.getEdit());
	}

	public String getText() throws BadLocationException {
		return getText(true);
	}

	public String getText(boolean expand) throws BadLocationException {
		return getText(0, getLength(), expand);
	}

	@Override
	public String getText(int offset, int length) throws BadLocationException {
		return getText(offset, length, true);
	}

	public String getText(int offset, int length, boolean expand) throws BadLocationException {
		String str = super.getText(offset, length);
		StringBuffer b=new StringBuffer();
		int n=0;
		int m=0;
		while(true) {
			n=str.indexOf(' ',n);
			if(n==-1) {
				n=str.length();
				break;
			}
			ImageIcon icon=getIcon(offset+n);
			if(icon == null || !expand);
			else {
				b.append(str.substring(m,n));
				b.append(icon.getDescription());
				m=n+1;
			}
			n++;
		}
		b.append(str.substring(m,n));
		return b.toString();
	}

	ImageIcon getIcon(int offset) {
		Element e=getCharacterElement(offset);
		AttributeSet s=e.getAttributes();
		ImageIcon icon=(ImageIcon)StyleConstants.getIcon(s);
		return icon;
	}
}
