/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jscl.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "Math",
id = "jscl.editor.ResetEngine")
@ActionRegistration(displayName = "#CTL_ResetEngine")
@ActionReferences({
	@ActionReference(path = "Menu/Math", position = 500)
})
@Messages("CTL_ResetEngine=Reset engine")
public final class ResetEngine implements ActionListener {

	public void actionPerformed(ActionEvent e) {
		MathManager.getDefault().reset();
	}
}
