package com.domiclipse.xots.model;

import org.eclipse.jdt.core.IType;

/**
 *  Filter used to decide if a Xots class should be accepted by the Xots builder. Generally the filter
 *  will exclude abstract classes or classes in jars etc and only accept classes in the nsf itself.
 *  
 * @author Keith Smillie
 *
 */
public interface ITypeFilter {
	
	/**
	 * Returns true if the Type should be accepted.
	 * 
	 * @param type
	 * @return
	 */
	public boolean accept(IType type) ;
}
