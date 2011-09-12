/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jscl.editor;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "Math",
id = "jscl.editor.PasteFromWiki")
@ActionRegistration(displayName = "#CTL_PasteFromWiki")
@ActionReferences({
	@ActionReference(path = "Menu/Math", position = 800)
})
@Messages("CTL_PasteFromWiki=Paste from Wiki")
public final class PasteFromWiki implements ActionListener {

	private final MathCookie context;

	public PasteFromWiki(MathCookie context) {
		this.context = context;
	}

	public void actionPerformed(ActionEvent ev) {
		try {
			context.pasteFromWiki();
		} catch (Exception ex) {
			Exceptions.printStackTrace(ex);
		}
	}
}
