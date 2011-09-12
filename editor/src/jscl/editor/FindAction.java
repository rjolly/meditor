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
id = "org.openide.actions.FindAction")
@ActionRegistration(displayName = "#CTL_FindAction")
@ActionReferences({
	@ActionReference(path = "Menu/Edit", position = 2100)
})
@Messages("CTL_FindAction=Find...")
public final class FindAction implements ActionListener {

	private final MathCookie context;

	public FindAction(MathCookie context) {
		this.context = context;
	}

	public void actionPerformed(ActionEvent ev) {
		context.find();
	}
}
