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
id = "jscl.editor.RunAction")
@ActionRegistration(iconBase = "jscl/editor/runProject.png",
displayName = "#CTL_RunAction")
@ActionReferences({
	@ActionReference(path = "Menu/Math", position = 200)
})
@Messages("CTL_RunAction=Run")
public final class RunAction implements ActionListener {

	private final MathCookie context;

	public RunAction(MathCookie context) {
		this.context = context;
	}

	public void actionPerformed(ActionEvent ev) {
		try {
			context.run();
		} catch (Exception ex) {
			Exceptions.printStackTrace(ex);
		}
	}
}
