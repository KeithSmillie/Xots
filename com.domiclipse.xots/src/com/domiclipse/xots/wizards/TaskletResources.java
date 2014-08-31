package com.domiclipse.xots.wizards;

import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IType;

/**
 * Represents the actual resources corresponding to the tasklet config/classname.
 * 
 * @author Keith Smillie
 *
 */
public class TaskletResources {
	private static final Logger logger = Logger.getLogger(TaskletResources.class.getName());
	
	private IType xotsType = null ;
	private IType otherType = null ;
	private IResource resource = null ;
	private JarFile jarFile = null ;
	
	public IType getXotsType() {
		return xotsType;
	}
	
	public void setXotsType(IType xotsType) {
		this.xotsType = xotsType;
	}
	
	public IType getOtherType() {
		return otherType;
	}
	
	public void setOtherType(IType otherType) {
		this.otherType = otherType;
	}
	
	public IResource getResource() {
		return resource;
	}
	
	public void setResource(IResource resource) {
		this.resource = resource;
	}
	
	public JarFile getJarFile() {
		return jarFile;
	}
	
	public void setJarFile(JarFile jarFile) {
		this.jarFile = jarFile;
	}
	
}
