/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jscl.editor;

import javax.swing.text.AbstractDocument;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

/**
 *
 * @author raphael
 */
class MathViewFactory implements ViewFactory {

	@Override
	public View create(Element elem) {
		String kind = elem.getName();
		if (kind != null) {
			if (kind.equals(AbstractDocument.ContentElementName)) {
				return new LabelView(elem);
			} else if (kind.equals(AbstractDocument.ParagraphElementName)) {
				return new ParagraphView(elem);
			} else if (kind.equals(AbstractDocument.SectionElementName)) {
				return new BoxView(elem, View.Y_AXIS);
			} else if (kind.equals(StyleConstants.ComponentElementName)) {
				return new ComponentView(elem);
			} else if (kind.equals(StyleConstants.IconElementName)) {
				return new IconView(elem);
			}
		}
		return new LabelView(elem);
	}

}
