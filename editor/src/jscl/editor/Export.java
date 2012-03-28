/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jscl.editor;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.io.IOException;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "Math",
id = "jscl.editor.Export")
@ActionRegistration(displayName = "#CTL_Export")
@ActionReferences({
	@ActionReference(path = "Menu/Math", position = 1200)
})
@Messages("CTL_Export=Export...")
public final class Export implements ActionListener {

	private final MathCookie context;

	public Export(MathCookie context) {
		this.context = context;
	}

	public void actionPerformed(ActionEvent ev) {
		try {
			context.export();
		} catch (IOException ex) {
			Exceptions.printStackTrace(ex);
		}
	}
}
