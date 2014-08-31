package com.domiclipse.xots.util;

import java.util.logging.Logger;
import java.util.logging.Level;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

public class MarkerUtils {
	private static final Logger logger = Logger.getLogger(MarkerUtils.class.getName());
	
	public static void addMarker(IFile file, String message, int lineNumber, int severity, String markerType) {
		try {
			if (file != null && file.exists()) {
				IMarker marker = file.createMarker(markerType);
				marker.setAttribute(IMarker.MESSAGE, message);
				marker.setAttribute(IMarker.SEVERITY, severity);
				if (lineNumber == -1) {
					lineNumber = 1;
				}
				marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);	
			}
		} 
		catch (CoreException e) {
			logger.log(Level.SEVERE, "Error adding Xots marker to: " + file, e);
		}
	}
	
	public static void removeMarkers(IFile file, String markerType) {
		try {
			if (file != null && file.exists()) {
				file.deleteMarkers(markerType, true, IResource.DEPTH_INFINITE);
			}	
		}
		catch (Exception e) {
			logger.log(Level.SEVERE, "Error removing Xots markers from: " + file, e);
		}
	}	
}
