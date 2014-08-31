package com.domiclipse.xots.wizards;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.wizard.Wizard;

import com.domiclipse.xots.configuration.IconNoteXotsConfigStore;
import com.domiclipse.xots.configuration.IconNoteXotsConfigStore2;
import com.domiclipse.xots.configuration.XotsDatabaseConfig;
import com.domiclipse.xots.configuration.XotsTaskletConfig;
import com.domiclipse.xots.model.IXotsConfigStore;

public class XotsConfigWizard extends Wizard {
	private static final Logger logger = Logger.getLogger(XotsConfigWizard.class.getName());

	/** The project the wizard is configuring */
	final private IProject project ;
	
	/** Model for wizards data */
	private final XotsWizardModel model ;
	
	/** The config page */
	private XotsConfigPage xotsConfigPage ;
	
	public XotsConfigWizard(IProject project) {
		this.project = project;
		setWindowTitle("XOTS Configuration");
		
		IXotsConfigStore store = new IconNoteXotsConfigStore2(project);
		model = new XotsWizardModel(project, store) ;
	}

	@Override
	public void addPages() {
				
		// find any new types that are not referenced in the xots config
		XotsDatabaseConfig xotsConfig = getModel().getXotsConfig() ;

		// get the known types
		List<IType> types = getModel().getTypes();
		
		// Update the config
		updateXotsConfig(xotsConfig, types);
		
		// Now actually add the pages...
		xotsConfigPage = new XotsConfigPage(getModel());
		addPage(xotsConfigPage);
	}
	
	/**
	 * Updates the xots config by adding disabled tasklets for all types that are not
	 * references in the current xots config.
	 * 
	 * @param xotsConfig
	 * @param types
	 * @return
	 */
	protected XotsDatabaseConfig updateXotsConfig(XotsDatabaseConfig xotsConfig, List<IType> types) {
		
		List<IType> unknownTypes = xotsConfig.getUnknownTypes(types);
		
		// add all the unknown types as disabled tasklets
		for (IType type : unknownTypes) {
			XotsTaskletConfig taskletConfig = new XotsTaskletConfig() ;
			taskletConfig.setEnabled(false);
			taskletConfig.setClassName(type.getFullyQualifiedName());
			xotsConfig.getTaskletConfigs().add(taskletConfig);
		}
		
		return xotsConfig ;
	}

	@Override
	public boolean performFinish() {
		try {
			// get the xots config
			XotsDatabaseConfig xotsConfig = getModel().getXotsConfig() ;
			
			// Put the new config in the store			
			getModel().getStore().setXotsDatabaseConfig(xotsConfig);
			
			return true;			
		}
		catch (Exception e) {
			logger.log(Level.SEVERE, "", e);
			return false ;
		}
	}

	public IProject getProject() {
		return project;
	}

	public XotsWizardModel getModel() {
		return model;
	}
}
