package com.domiclipse.xots.model;

import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Represents the Xots configuration of a Notes database.
 * 
 * @author Keith Smillie
 *
 */
public interface IXotsDatabaseConfig {
	
	public void setTaskletConfigs(List<IXotsTaskletConfig> taskletConfigs) ;
	
	public List<IXotsTaskletConfig> getTaskletConfigs() ;
	
	public IXotsTaskletConfig getTaskletConfig(String className) ;

}
