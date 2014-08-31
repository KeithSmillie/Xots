package com.domiclipse.xots.wizards;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import com.domiclipse.xots.builder.XotsBuilder;
import com.domiclipse.xots.configuration.XotsDatabaseConfig;
import com.domiclipse.xots.configuration.XotsTaskletConfig;
import com.domiclipse.xots.filter.XotsTypeFilter;
import com.domiclipse.xots.model.IXotsConfigStore;
import com.domiclipse.xots.util.JDTUtils;

public class XotsWizardModel {
	private static final Logger logger = Logger.getLogger(XotsWizardModel.class.getName());
	
	/** The current project the wizard is acting on */
	private final IProject project ;
	
	private final IJavaProject javaProject ;
	
	private final IXotsConfigStore store ;
	
	private XotsDatabaseConfig xotsConfig ;
	
	/** The actual resources corresponding to the tasklet config */
	Map<XotsTaskletConfig, TaskletResources> taskletResourceMap = new HashMap<XotsTaskletConfig, TaskletResources>() ;; 

	/** All the xots types in the project obtained via the jdt */
	private List<IType> types = null ;
		
	public XotsWizardModel(IProject project, IXotsConfigStore store) {
		this.project = project;
		this.javaProject = JavaCore.create(project);
		this.store = store;
	}

	public TaskletResources getTaskletResources(XotsTaskletConfig tasklet) {
		TaskletResources taskletResource = null ;
		
		if (taskletResourceMap.containsKey(tasklet)) {
			// return the existing resource
			return taskletResourceMap.get(tasklet);
		}
		else {
			// create a new resource
			taskletResource = new TaskletResources() ;
			
			// locate the xots type if any
			for (IType xotsType : getTypes()) {
				if (xotsType.getFullyQualifiedName().equals(tasklet.getClassName())) {
					taskletResource.setXotsType(xotsType);
					taskletResource.setResource(xotsType.getResource());
				}
			}
			
			if (taskletResource.getXotsType() == null) {
				// see if we can find any type
				try {
					IType otherType = JavaCore.create(getProject()).findType(tasklet.getClassName());
					if (otherType != null) {
						taskletResource.setOtherType(otherType);
						taskletResource.setResource(otherType.getResource());						
					}

				}
				catch (JavaModelException e) {
					logger.log(Level.SEVERE, "", e) ;
				}
			}
			
			taskletResourceMap.put(tasklet, taskletResource);
		}
		
		return taskletResource ;
	}
	
	public IProject getProject() {
		return project;
	}

	public IXotsConfigStore getStore() {
		return store;
	}

	public List<IType> getTypes() {
		if (types == null) {
			IJavaProject javaProject = JavaCore.create(getProject());
			types = JDTUtils.getSubtypes(javaProject, XotsBuilder.XOTS_CLASS_NAME, new XotsTypeFilter(),new NullProgressMonitor());			
		}
		return types;
	}

	public void setTypes(List<IType> types) {
		this.types = types;
	}

	public XotsDatabaseConfig getXotsConfig() {
		if (xotsConfig == null) {
			xotsConfig = getStore().getXotsDatabaseConfig();
			if (xotsConfig == null) {
				xotsConfig = new XotsDatabaseConfig() ;
			}
		}
		return xotsConfig;
	}

	public void setXotsConfig(XotsDatabaseConfig xotsConfig) {	
		this.xotsConfig = xotsConfig;
	}

	public IJavaProject getJavaProject() {
		return javaProject;
	}
}
