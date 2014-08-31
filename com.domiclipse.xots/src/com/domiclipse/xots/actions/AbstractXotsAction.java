package com.domiclipse.xots.actions;

import java.util.Iterator;
import java.util.logging.Logger;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public abstract class AbstractXotsAction implements IObjectActionDelegate {
	
	private static final Logger logger = Logger.getLogger(AbstractXotsAction.class.getName());
	
	protected ISelection selection;

	/**
	 * Run an action on the selected project.
	 * 
	 * @param project
	 */
	protected abstract void runOnProject(IProject project) ;
	
	@Override
	public void run(IAction action) {
		if (selection instanceof IStructuredSelection) {
			for (Iterator it = ((IStructuredSelection) selection).iterator(); it.hasNext();) {
				Object element = it.next();
				IProject project = null;
				if (element instanceof IProject) {
					project = (IProject) element;
				}
				else if (element instanceof IAdaptable) {
					project = (IProject) ((IAdaptable) element).getAdapter(IProject.class);
				}
				
				if (project != null) {
					runOnProject(project);
				}
			}
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart part) {
	}
}
