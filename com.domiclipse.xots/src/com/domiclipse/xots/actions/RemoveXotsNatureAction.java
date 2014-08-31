package com.domiclipse.xots.actions;

import java.text.MessageFormat;
import java.util.logging.Logger;

import org.eclipse.core.resources.IProject;

import com.domiclipse.xots.nature.XotsNature;
import com.domiclipse.xots.util.NatureUtils;

public class RemoveXotsNatureAction extends AbstractXotsAction {
	
	private static final Logger logger = Logger.getLogger(RemoveXotsNatureAction.class.getName());

	@Override
	protected void runOnProject(IProject project) {
		logger.info(MessageFormat.format("Removing nature: {0} from project {1}", XotsNature.NATURE_ID, project.getFullPath()));
		NatureUtils.removeNature(project, XotsNature.NATURE_ID);
	}
}
