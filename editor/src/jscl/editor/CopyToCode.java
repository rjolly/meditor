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
id = "jscl.editor.CopyToCode")
@ActionRegistration(displayName = "#CTL_CopyToCode")
@ActionReferences({
	@ActionReference(path = "Menu/Math", position = 1000)
})
@Messages("CTL_CopyToCode=Copy to code")
public final class CopyToCode implements ActionListener {

	private final MathCookie context;

	public CopyToCode(MathCookie context) {
		this.context = context;
	}

	public void actionPerformed(ActionEvent ev) {
		try {
			context.copyToCode();
		} catch (Exception ex) {
			Exceptions.printStackTrace(ex);
		}
	}
}
