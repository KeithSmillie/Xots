package com.domiclipse.xots.util;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;

/**
 * Utilities for manipulating Eclipse project natures.
 * 
 * @author Keith Smillie
 *
 */
public class NatureUtils {
	private static final Logger logger = Logger.getLogger(NatureUtils.class.getName());
		
	/**
	 * Toggle the nature on the specified project.
	 * 
	 * @param natureId
	 * @return 
	 */
	public static boolean toggleNature(IProject project, String natureId) {
		
		try {
			if (project.hasNature(natureId)) {
				removeNature(project, natureId);
				return false ;
			}
			else {
				addNature(project, natureId);
				return true ;
			}
		}
		catch (CoreException e) {
			logger.log(Level.SEVERE, MessageFormat.format("Error toggling nature: {0} on project: {1}", natureId, project.getFullPath()) , e);			
		}
		
		return false ;
	}
	
	/**
	 * Add the nature to the specified project.
	 * 
	 * @param natureId
	 * @return
	 */
	public static boolean addNature(IProject project, String natureId) {
		try {

			IProjectDescription description = project.getDescription();
			String[] natures = description.getNatureIds();			
			String[] newNatures = new String[natures.length + 1];
			System.arraycopy(natures, 0, newNatures, 0, natures.length);
			newNatures[natures.length] = natureId;
			description.setNatureIds(newNatures);
			project.setDescription(description, null);
		}
		catch (Exception e) {
			logger.log(Level.SEVERE, MessageFormat.format("Error adding nature: {0} to project: {1}", natureId, project.getFullPath()) , e);
		}
		return false ;
	}	
	
	/**
	 * Remove the nature to the specified project. 
	 * @param natureId
	 * @return
	 */
	public static boolean removeNature(IProject project, String natureId) {
		try {
			IProjectDescription description = project.getDescription();
			String[] natures = description.getNatureIds();

			for (int i = 0; i < natures.length; ++i) {
				if (natureId.equals(natures[i])) {
					String[] newNatures = new String[natures.length - 1];
					System.arraycopy(natures, 0, newNatures, 0, i);
					System.arraycopy(natures, i + 1, newNatures, i, natures.length - i - 1);
					description.setNatureIds(newNatures);
					project.setDescription(description, null);
				}
			}			
		}
		catch (Exception e) {
			logger.log(Level.SEVERE, MessageFormat.format("Error removing nature: {0} from project: {1}", natureId, project.getFullPath()) , e);			
		}
		
		return false ;
	}	
	
}
