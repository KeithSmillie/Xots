package com.domiclipse.xots.model;

/**
 * Represents the Xots configuration of a single tasklet.
 * 
 * @author Keith Smillie
 *
 */
public interface IXotsTaskletConfig {
	
	/**
	 * Is the tasklet enabled to run on the server.
	 * 
	 * @param enabled
	 */
	public void setEnabled(boolean enabled) ;
	
	/**
	 * Is the tasklet enabled to run on the server.
	 * 
	 * @param enabled
	 */
	public boolean isEnabled() ;	
	
	
	/**
	 * The fully qualified name of the Xots class.
	 * 
	 * @param enabled
	 */
	public void setClassName(String className) ;
	
	/**
	 * The fully qualified name of the Xots class.
	 * 
	 * @param enabled
	 */
	public String getClassName() ;

}
