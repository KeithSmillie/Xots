package com.domiclipse.xots.actions;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;

import com.domiclipse.xots.wizards.XotsConfigWizard;

public class XotsConfigAction extends AbstractXotsAction {

	private static final Logger logger = Logger.getLogger(XotsConfigAction.class.getName());	
	
	@Override
	protected void runOnProject(IProject project) {
		try {
			WizardDialog dialog = new WizardDialog(Display.getCurrent().getActiveShell(), new XotsConfigWizard(project));
			dialog.open(); 
		}
		catch (Exception e) {
			logger.log(Level.SEVERE, "Error running Xots configuration wizard", e);
		}
	}
	
}
