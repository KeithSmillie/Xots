package com.domiclipse.xots.model;

import org.eclipse.core.resources.IFile;

import com.domiclipse.xots.configuration.XotsDatabaseConfig;

/**
 * Represents a place to store the xots configuration of a database.
 * 
 * @author Keith Smillie
 *
 */
public interface IXotsConfigStore {
	
	/**
	 * Get the Xots configuration.
	 * 
	 * @return
	 */
	public XotsDatabaseConfig getXotsDatabaseConfig() ;
	
	/**
	 * Set the new Xots configuration.
	 * 
	 * @param xotsDatabaseConfig
	 */
	public void setXotsDatabaseConfig(XotsDatabaseConfig xotsDatabaseConfig) ;
	
	/**
	 * Return the underlying eclipse resource used by the store. May return null if the 
	 * store is not backed by a resource.
	 * It is this resource that the builder will place any errors, warnings etc on.
	 * 
	 * @return
	 */
	public IFile getFile() ;

}
