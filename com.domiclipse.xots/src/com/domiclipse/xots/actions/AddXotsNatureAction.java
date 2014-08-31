package com.domiclipse.xots.actions;

import java.text.MessageFormat;
import java.util.logging.Logger;

import org.eclipse.core.resources.IProject;

import com.domiclipse.xots.nature.XotsNature;
import com.domiclipse.xots.util.NatureUtils;

public class AddXotsNatureAction extends AbstractXotsAction {

	private static final Logger logger = Logger.getLogger(AddXotsNatureAction.class.getName());	
	
	@Override
	protected void runOnProject(IProject project) {
		logger.info(MessageFormat.format("Adding nature: {0} to project {1}", XotsNature.NATURE_ID, project.getFullPath()));
		NatureUtils.addNature(project, XotsNature.NATURE_ID);
	}
	
}
