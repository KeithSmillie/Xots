package com.domiclipse.xots.wizards;

import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.domiclipse.xots.Activator;
import com.domiclipse.xots.builder.XotsBuilder;
import com.domiclipse.xots.configuration.XotsTaskletConfig;

public class XotsCellLabelProvider extends StyledCellLabelProvider {
	private static final Logger logger = Logger.getLogger(XotsCellLabelProvider.class.getName());
	
	
	private final Columns columnType;
	
	WorkbenchLabelProvider workbenchLabelProvider = new WorkbenchLabelProvider() ;
	
	public static final Image unknownIcon = Activator.getImageDescriptor("icons/unknown_obj.gif").createImage();	
	public static final Image jarIcon = Activator.getImageDescriptor("icons/jar.gif").createImage();	
	
	private final ImageDescriptor errorOverlayImage = Activator.getImageDescriptor("icons/overlay/error_ovr.gif");
	private final ImageDescriptor warnOverlayImage = Activator.getImageDescriptor("icons/overlay/warn_ovr.gif");

	private final XotsWizardModel model;	

	
	public XotsCellLabelProvider(Columns columnType, XotsWizardModel model ) {
		this.columnType = columnType;
		this.model = model;
	}
			
	@Override
	public void update(ViewerCell cell) {
		XotsTaskletConfig tasklet = (XotsTaskletConfig) cell.getElement();
		TaskletResources resources = getModel().getTaskletResources(tasklet);
	
		updateText(cell, tasklet, resources);
		updateImage(cell, tasklet, resources);
		
		super.update(cell);
	}
	
	protected void updateText(ViewerCell cell, XotsTaskletConfig tasklet, TaskletResources resources) {
		String text = "" ;
		if (getColumnType() == Columns.COLUMN_CLASSNAME) {
			text = tasklet.getClassName();
		}
		else if (getColumnType() == Columns.COLUMN_RESOURCE) {
			if (resources.getXotsType() != null) {
				IResource resource = resources.getResource() ;
				if (resource != null) {
					text = resource.getProjectRelativePath().toPortableString();
				}
				else {
					try {
						IPackageFragment packageFragment = resources.getXotsType().getPackageFragment();
						IJavaElement parent = packageFragment.getParent();
						if (parent instanceof JarPackageFragmentRoot) {
							JarPackageFragmentRoot root = (JarPackageFragmentRoot) parent ;
							IPath path = root.getPath();
							String segment = path.segment(path.segmentCount()-1);
							text = segment;
						}
						else {
							text = "no jar file" ;
						}
					}
					catch (Exception e) {
						logger.log(Level.SEVERE, "  ", e);
					}
				}
			}
			else {
				text = "no xots type";
			}
		}
		else if (getColumnType() == Columns.COLUMN_TEST) {
			text = tasklet.getClassName();
		}
		
		cell.setText(text);
	}
	
	protected void updateImage(ViewerCell cell, XotsTaskletConfig tasklet, TaskletResources resources) {
		Image image = null ;
		if (getColumnType() == Columns.COLUMN_CLASSNAME) {
			if (resources.getXotsType() != null) {
				image = workbenchLabelProvider.getImage(resources.getXotsType());	
			}
			else {
				image = unknownIcon ;
				try {
					IType type = getModel().getJavaProject().findType(tasklet.getClassName());
					if (type != null) {
						image = workbenchLabelProvider.getImage(type);
					}
				}
				catch (Exception e) {
					logger.log(Level.SEVERE, "", e);
				}
				image = new DecorationOverlayIcon(image, errorOverlayImage, IDecoration.BOTTOM_RIGHT).createImage();
			}
		}
		else if (getColumnType() == Columns.COLUMN_RESOURCE) {
			if (resources.getXotsType() != null) {
				IResource resource = resources.getXotsType().getResource() ;
				if (resource != null) {
					image = workbenchLabelProvider.getImage(resource);	
				}		
				else {
					try {
						IPackageFragment packageFragment = resources.getXotsType().getPackageFragment();
						IJavaElement parent = packageFragment.getParent();
						if (parent instanceof JarPackageFragmentRoot) {
							image = jarIcon;
						}
					}
					catch (Exception e) {
						logger.log(Level.SEVERE, "  ", e);
					}
				}
			}
			else {
				image = unknownIcon ;
			}
		}
		
		cell.setImage(image);
	}
	
	public Columns getColumnType() {
		return columnType;
	}

	public enum Columns {
		COLUMN_CLASSNAME ,
		COLUMN_RESOURCE ,
		COLUMN_TEST ;
	}

	@Override
	public String getToolTipText(Object obj) {
		String tooltip = "" ;
		XotsTaskletConfig tasklet = (XotsTaskletConfig) obj;

		IType xotsType = null ;
		for (IType type : getModel().getTypes()) {
			if (type.getFullyQualifiedName().equals(tasklet.getClassName())) {
				xotsType = type ;
			}
		}
		
		if (getColumnType() == Columns.COLUMN_CLASSNAME) {
			if (xotsType == null) {
				tooltip = "Error: " + tasklet.getClassName() + " is not a subclass of " + XotsBuilder.XOTS_CLASS_NAME;				
			}
			else {
				tooltip = tasklet.getClassName();
			}
		}
		else if (getColumnType() == Columns.COLUMN_RESOURCE) {
			if (xotsType != null) {
				IResource resource = xotsType.getResource() ;
				if (resource != null) {
					tooltip = resource.getFullPath().toPortableString() ;	
				}		
				else {
					try {
						IPackageFragment packageFragment = xotsType.getPackageFragment();
						IJavaElement parent = packageFragment.getParent();
						if (parent instanceof JarPackageFragmentRoot) {
							JarPackageFragmentRoot root = (JarPackageFragmentRoot) parent ;
							tooltip = root.getPath().toPortableString();
						}
					}
					catch (Exception e) {
						logger.log(Level.SEVERE, "  ", e);
					}
				}
			}
		}
		else if (getColumnType() == Columns.COLUMN_TEST) {
			tooltip = "My test tooltip";
		}
		
		return tooltip;
	}

	public XotsWizardModel getModel() {
		return model;
	}
}
