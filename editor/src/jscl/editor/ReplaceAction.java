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
import org.openide.util.NbBundle.Messages;

@ActionID(category = "Edit",
id = "jscl.editor.ReplaceAction")
@ActionRegistration(displayName = "#CTL_ReplaceAction")
@ActionReferences({
	@ActionReference(path = "Menu/Edit", position = 2200)
})
@Messages("CTL_ReplaceAction=Replace...")
public final class ReplaceAction implements ActionListener {

	private final MathCookie context;

	public ReplaceAction(MathCookie context) {
		this.context = context;
	}

	public void actionPerformed(ActionEvent ev) {
		context.replace();
	}
}
