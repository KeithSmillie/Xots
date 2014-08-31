package com.domiclipse.xots.builder;

import java.util.logging.Logger;

import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;

/**
 * Visits a resource delta and sets the accepted flag if any of the changes are
 * to files that affect the xots configuration, eg .java files or the icon note.
 * 
 * @author Keith Smillie
 *
 */
public class XotsResourceVisitor implements IResourceDeltaVisitor {
	private static final Logger logger = Logger.getLogger(XotsResourceVisitor.class.getName());

	/** Flag indicating the delta is of interest to us */
	private boolean accepted = false ;
	
	@Override
	public boolean visit(IResourceDelta resourceDelta) throws CoreException {
		String fullPath = resourceDelta.getProjectRelativePath().toPortableString();
		
		if (fullPath.endsWith(".java")){
			// A java file has changed
			setAccepted(true) ;
		}
		else if (fullPath.equals(XotsBuilder.ICON_NOTE_PATH)) {
			// The icon note has changed
			setAccepted(true) ;
		}
		
		// Only need to continue visiting if we have not found anything
		return !isAccepted();
	}

	public boolean isAccepted() {
		return accepted;
	}

	public void setAccepted(boolean accepted) {
		this.accepted = accepted;
	}
}
