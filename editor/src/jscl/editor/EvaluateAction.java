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
id = "jscl.editor.EvaluateAction")
@ActionRegistration(iconBase = "jscl/editor/find.gif",
displayName = "#CTL_EvaluateAction")
@ActionReferences({
	@ActionReference(path = "Menu/Math", position = 300)
})
@Messages("CTL_EvaluateAction=Evaluate")
public final class EvaluateAction implements ActionListener {

	private final MathCookie context;

	public EvaluateAction(MathCookie context) {
		this.context = context;
	}

	public void actionPerformed(ActionEvent ev) {
		try {
			context.evaluate();
		} catch (Exception ex) {
			Exceptions.printStackTrace(ex);
		}
	}
}
