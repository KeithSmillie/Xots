package com.domiclipse.xots.util;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import com.domiclipse.xots.model.ITypeFilter;

/**
 * Some utilities for working with the Eclipse JDT.
 * 
 * @author Keith Smillie
 *
 */
public class JDTUtils {
	private static final Logger logger = Logger.getLogger(JDTUtils.class.getName());
	
	/**
	 * Parse a compilation unit and return the AST.
	 * 
	 * @param unit
	 * @return
	 */
	public static CompilationUnit parse(ICompilationUnit unit) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit);
		parser.setResolveBindings(true);
		return (CompilationUnit) parser.createAST(null);
	}	
	
	/**
	 * Locates all classes within the specified project that extend the given class.
	 * 
	 * @param javaProject
	 * @param className
	 * @return
	 */
	public static List<IType> getSubtypes(IJavaProject javaProject, String className, IProgressMonitor monitor) {
		return getSubtypes(javaProject, className, null, monitor);
	}
	
	/**
	 * Locates all classes within the specified project that extend the given class using the supplied filter to
	 * restrict the returned types.
	 * 
	 * @param javaProject
	 * @param className
	 * @param filter
	 * @return
	 */
	public static List<IType> getSubtypes(IJavaProject javaProject, String className, ITypeFilter filter, IProgressMonitor monitor) {
		List<IType> types = new ArrayList<IType>();
		try {
			monitor.beginTask("Locating XOTS subclasses", 1);
			
			// Locate the class
			IType xotsType = javaProject.findType(className);
			if (xotsType != null) {
				
				ITypeHierarchy typeHierarchy = xotsType.newTypeHierarchy(javaProject, new SubProgressMonitor(monitor, 1));
				IType[] subtypes = typeHierarchy.getAllSubtypes(xotsType);
				for (IType type : subtypes) {
					if (filter != null) {
						// Have a filter so only add if the filter accepts						
						if (filter.accept(type)) {
							types.add(type);
						}
					}
					else {
						// No filter so add to collection
						types.add(type);
					}					
				}	
			}
		}
		catch (Exception e) {
			logger.log(Level.SEVERE, "Error locating subtypes of: " + className + " in project: " + javaProject.getProject().getFullPath(), e);
		}
		finally {
			monitor.done();
		}

		return types;
	}	
		
}
