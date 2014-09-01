package com.domiclipse.xots.builder;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;

import com.domiclipse.xots.configuration.IconNoteXotsConfigStore2;
import com.domiclipse.xots.configuration.XotsDatabaseConfig;
import com.domiclipse.xots.configuration.XotsTaskletConfig;
import com.domiclipse.xots.filter.XotsTypeFilter;
import com.domiclipse.xots.model.IXotsConfigStore;
import com.domiclipse.xots.model.IXotsTaskletConfig;
import com.domiclipse.xots.preferences.XotsPreferenceConstants;
import com.domiclipse.xots.preferences.XotsPreferenceInitializer;
import com.domiclipse.xots.util.JDTUtils;
import com.domiclipse.xots.util.MarkerUtils;
import com.ibm.designer.domino.ide.resources.DominoResourcesPlugin;
import com.ibm.designer.domino.ide.resources.project.IDominoDesignerProject;

/**
 * An incremental project builder that processes XOTS classes. The builder
 * searches for all Java classes that extend XotsBaseTasklet.
 * 
 * If any are found the fully qualified names of the classes are written to the
 * $Xots item in the icon note of the database.
 * 
 * @author Keith Smillie
 * 
 */
public class XotsBuilder extends IncrementalProjectBuilder {

	private static final Logger logger = Logger.getLogger(XotsBuilder.class.getName());

	/** The id of the Xots builder */
	public static final String BUILDER_ID = "com.domiclipse.xots.XotsBuilder";	
	
	/** Name of the item on the icon note holding xots config */
	public static final String XOTS_ITEM_NAME = "$Xots";
	
	private static final String MARKER_TYPE = "com.domiclipse.xots.xotsProblem";	

	/** The class (and subclasses) that this builder will search for */
	public static final String XOTS_CLASS_NAME = "org.openntf.domino.xots.XotsBaseTasklet";	
	

	@SuppressWarnings("rawtypes")
	@Override
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		try {
			monitor.beginTask("Running XOTS builder", 1);
						
			IDominoDesignerProject ddeProject = DominoResourcesPlugin.getDominoDesignerProject(getProject());
			 
			// Ignore initial builds where the dde project isn't ready
			// otherwise the JDT goes into an infinite loop when we search for types
			if (ddeProject != null && ddeProject.isProjectInitialized()) {
				
				if (kind == IncrementalProjectBuilder.AUTO_BUILD) {
					// auto build - only build if any xots relevant files have changed
					IResourceDelta delta = getDelta(getProject());
					XotsResourceVisitor resourceVisitor = new XotsResourceVisitor();
					delta.accept(resourceVisitor);
					
					if (resourceVisitor.isAccepted()) {
						logger.fine(MessageFormat.format("XOTS builder starting AUTO build on project: {0}", getProject().getName()) );
						doBuild(new SubProgressMonitor(monitor, 1)) ;
					}
					else {
						monitor.worked(1);
					}
				}
				else {
					// any other kind of build treat as a full build				
					if (getProject().isOpen() && getProject().isAccessible()) {
						logger.fine(MessageFormat.format("XOTS builder starting FULL build on project: {0}", getProject().getName()) );									
						doBuild(new SubProgressMonitor(monitor, 1)) ;
					}
				}
				
				return null ;
			}
		}
		catch (Exception e) {
			logger.log(Level.SEVERE, "Error in XOTS builder: " + e.getLocalizedMessage(), e);
		}
		finally {
			monitor.done();
		}

		return null;
	}
	
	public void doBuild(IProgressMonitor monitor) {
		long buildStart = System.currentTimeMillis();
		try {
			logger.fine(MessageFormat.format("Building XOTS Project: {0}", getProject().getFullPath()) );			
			monitor.beginTask("Building project", 2);
						
			// Get all the classes that extend the XOTS super class
			monitor.subTask("Locating all XOTS classes on classpath");
			IJavaProject javaProject = JavaCore.create(getProject());
			List<IType> xotsTypes = JDTUtils.getSubtypes(javaProject, XOTS_CLASS_NAME, new XotsTypeFilter(), new SubProgressMonitor(monitor, 1));
						
			// Create somewhere to put the config
			IXotsConfigStore configStore = new IconNoteXotsConfigStore2(getProject()) ;
			
			// Remove any existing markers
			IFile iconResource = configStore.getFile() ;
			MarkerUtils.removeMarkers(iconResource, MARKER_TYPE);
			
			// Get any existing xots config
			XotsDatabaseConfig xotsDatabaseConfig = configStore.getXotsDatabaseConfig();
			if (xotsDatabaseConfig != null) {
				// already have a config store so update with new tasklets
				// and check existing tasklet configs are valid and exist
				for (IType xotType : xotsTypes) {
					String fqn = xotType.getFullyQualifiedName();
					
					// See if ths tasklet is already in config
					IXotsTaskletConfig existingTaskletConfig = xotsDatabaseConfig.getTaskletConfig(fqn) ;
					if (existingTaskletConfig == null) {
						// not in config so add new tasklet to config in a disabled state
						XotsTaskletConfig taskletConfig = new XotsTaskletConfig() ;
						taskletConfig.setEnabled(isEnableNewXotsCLasses());
						taskletConfig.setClassName(xotType.getFullyQualifiedName());
						xotsDatabaseConfig.getTaskletConfigs().add(taskletConfig);
					}
				}	
				
				// Examine all the tasklets in the config and check they are valid/exist.
				// Either flag invalid ones with a marker or remove them from the config.
				List<XotsTaskletConfig> removals = new ArrayList<XotsTaskletConfig>() ;
				for (XotsTaskletConfig taskletConfig : xotsDatabaseConfig.getTaskletConfigs()) {
					String className = taskletConfig.getClassName();
					IType thisType = null ;
					
					for (IType xotsType : xotsTypes) {
						String fqn = xotsType.getFullyQualifiedName();
						if (fqn.equals(className)) {
							thisType = xotsType ;
							break ;
						}
					}
					
					if (thisType == null) {
						// This is a tasklet config that refers to a class that is not a 
						// subclass of the xots superclass
						if (isRemoveMissingXotsCLasses()) {
							// Mark for removal from config
							removals.add(taskletConfig);
						}
						else {
							if (iconResource != null && iconResource.exists()) {
								// Add a marker to the icon note.
								logger.warning("Bad tasklet config: " + className);
								MarkerUtils.addMarker(
										iconResource, 
										"Xots tasklet: " + className + " is not a subclass of " + XOTS_CLASS_NAME, 
										1, 
										IMarker.SEVERITY_WARNING,
										MARKER_TYPE);								
							}
						}
					}
				}
				
				// Remove the removals, if any
				xotsDatabaseConfig.getTaskletConfigs().removeAll(removals);
			}
			else {
				// no xots config store so create a new one but only if there are any xots classes
				if (!xotsTypes.isEmpty()) {
					xotsDatabaseConfig = new XotsDatabaseConfig() ;
					
					for (IType xotType : xotsTypes) {
						XotsTaskletConfig taskletConfig = createConfigForType(xotType);
						xotsDatabaseConfig.getTaskletConfigs().add(taskletConfig);
					}	
				}
			}
			
			// Save the XOTS configuration
			monitor.subTask("Saving XOTS configuration");
			configStore.setXotsDatabaseConfig(xotsDatabaseConfig);
			monitor.worked(1);			
		}
		catch (Exception e) {
			logger.log(Level.SEVERE, "Error building project", e);
		}
		finally {
			monitor.done();
		}
		
		long buildDuration = System.currentTimeMillis() - buildStart;
		logger.fine(MessageFormat.format("XOTS Project built in {0} ms", buildDuration) );		
	}
	
	private XotsTaskletConfig createConfigForType(IType type) {
		XotsTaskletConfig taskletConfig = new XotsTaskletConfig() ;
		taskletConfig.setEnabled(true);
		taskletConfig.setClassName(type.getFullyQualifiedName());
		
		return taskletConfig ;
	}
	
	private boolean isEnableNewXotsCLasses() {
		return Platform.getPreferencesService().getBoolean("com.domiclipse.xots", XotsPreferenceConstants.ENABLE_NEW_XOTS, XotsPreferenceInitializer.ENABLE_NEW_XOTS_DEFAULT, null); 		
	}
	
	private boolean isRemoveMissingXotsCLasses() {
		return Platform.getPreferencesService().getBoolean("com.domiclipse.xots", XotsPreferenceConstants.REMOVE_MISSING_XOTS, XotsPreferenceInitializer.REMOVE_MISSING_XOTS_DEFAULT, null);
	}	
	

}
