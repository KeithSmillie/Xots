package com.domiclipse.xots.filter;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IType;

import com.domiclipse.xots.model.ITypeFilter;


/**
 * Filter for Xots Types which returns only types we are interested in and ignores abstract and non-local types.
 * 
 * @author Keith Smillie
 *
 */
public class XotsTypeFilter implements ITypeFilter {
	private static final Logger logger = Logger.getLogger(XotsTypeFilter.class.getName());

	@Override
	public boolean accept(IType type) {
		try {
			int flags = type.getFlags();
			
			// Reject abstract types
			if (Flags.isAbstract(flags)) {
				logger.info("Rejecting type: " + type.getFullyQualifiedName() + ": abstract class");
				return false ;
			}
			
			// Reject types which are not backed by a resource in the db, ie they are not local
			/*IResource resource = type.getResource() ;
			if (resource == null || !resource.exists()) {
				System.out.println("Rejecting type: " + type.getFullyQualifiedName() + ": no resource");				
				return false ;
			}*/			
		}
		catch (Exception e) {
			logger.log(Level.SEVERE, "Error filtering Type collection", e);
		}

		logger.info("Accepting type: " + type.getFullyQualifiedName() );
		return true;
	}
	

}
