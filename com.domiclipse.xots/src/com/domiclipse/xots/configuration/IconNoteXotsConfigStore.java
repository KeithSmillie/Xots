package com.domiclipse.xots.configuration;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.domiclipse.xots.model.IXotsConfigStore;
import com.domiclipse.xots.util.XMLUtils;

public class IconNoteXotsConfigStore implements IXotsConfigStore {
	private static final Logger logger = Logger.getLogger(IconNoteXotsConfigStore.class.getName());
	
	/** Location of the Icon Note */
	public static final String ICON_NOTE_PATH = "Resources/IconNote";
	
	/** Name of the item on the icon note holding xots config */
	public static final String XOTS_ITEM_NAME = "$Xots";	
	
	private final IProject project;

	public IconNoteXotsConfigStore(IProject project) {
		this.project = project;
	}
	

	private List<String> getXotsClassNames() {
		List<String> classNames = new ArrayList<String>() ;
		try {
			// Get the icon note resource
			final IFile iconFile = getProject().getFile(ICON_NOTE_PATH);
			if (iconFile.exists()) {
				// parse the icon note dxl
				Document document = parseIconNote(iconFile);
				
				// Locate the xots item, if any
				Element xotsElement = getXotsElement(document);
				
				if (xotsElement != null) {
					NodeList childNodes = xotsElement.getChildNodes();
					if (childNodes.getLength() == 1) {
						Element item = (Element) childNodes.item(0);
						if (item.getNodeName().equals("textlist")) {
							NodeList childNodes2 = item.getChildNodes();
							for (int x = 0 ; x < childNodes2.getLength(); x++) {
								Element element = (Element) childNodes2.item(x);
								classNames.add(element.getTextContent());
							}
						}
						
						if (item.getNodeName().equals("text")) {
							classNames.add(item.getTextContent());
						}
					}
				}
			}
			else {
				logger.log(Level.SEVERE, "Error Icon Note does not exist");
			}
		}
		catch (Exception e) {
			logger.log(Level.SEVERE, "Error updating Icon Note: ", e);
		}
		
		return classNames ;
	}


	private void setXotsClassNames(List<String> classNames) {
		try {
			Collections.sort(classNames);

			// Get the icon note resource
			final IFile iconFile = getProject().getFile(ICON_NOTE_PATH);
			if (iconFile.exists()) {
				// parse the icon note dxl
				Document document = parseIconNote(iconFile);
				
				// Locate the xots item, if any				
				Element xotsElement = getXotsElement(document);
								
				if (classNames.isEmpty() && xotsElement != null ) {
					// has item, no classes -> remove item
					document.getDocumentElement().removeChild(xotsElement);
				}				
				else if (!classNames.isEmpty()) {
					// got some classes so replace the node
					Element itemElement = document.createElement("item");
					itemElement.setAttribute("name", XOTS_ITEM_NAME);
					
					Element textlistElement = document.createElement("textlist");
					itemElement.appendChild(textlistElement);
					
					for (String className : classNames) {
						Element textElement = document.createElement("text");
						textElement.setTextContent(className);
						
						textlistElement.appendChild(textElement);
					}
					
					if (xotsElement == null) {
						// add new xots item
						document.getDocumentElement().appendChild(itemElement);						
					}
					else {
						// replace existing xots item						
						document.getDocumentElement().replaceChild(itemElement, xotsElement);
					}
				}				

				// serialize the xml and write it back to the icon note
				final String newXML = XMLUtils.serializeDOM(document);
				IWorkspaceRunnable runnable = new IWorkspaceRunnable() {					
					@Override
					public void run(IProgressMonitor arg0) throws CoreException {
						try {
							iconFile.setContents(new ByteArrayInputStream(newXML.getBytes()), true, true, null);	
						}
						catch (Exception e) {
							logger.log(Level.SEVERE, "Error saving icon note.", e);
						}
					}
				};
				runnable.run(null);
			}
			else {
				logger.log(Level.SEVERE, "Error Icon Note does not exist");
			}
		}
		catch (Exception e) {
			logger.log(Level.SEVERE, "Error updating Icon Note: ", e);
		}
	}
	
	protected Document parseIconNote(IFile iconFile) {
		try {
			// Parse the xml
			iconFile.getContents();

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			DocumentBuilder db = dbf.newDocumentBuilder();

			// parse the icon note dxl
			Document document = db.parse(iconFile.getContents());
			
			return document ;
		}
		catch (Exception e) {
			logger.log(Level.SEVERE, "", e);
		}

		return null ;
	}
	
	protected Element getXotsElement(Document document) {
		// Locate the xots item, if any
		Element xotsElement = null;
		NodeList items = document.getElementsByTagName("item");
		for (int x = 0; x < items.getLength(); x++) {
			Element element = (Element) items.item(x);
			if (element.getAttribute("name").equals(XOTS_ITEM_NAME)) {
				xotsElement = element;
				break;
			}
		}
		
		return xotsElement ;
	}

	public IProject getProject() {
		return project;
	}

	@Override
	public XotsDatabaseConfig getXotsDatabaseConfig() {
		try {
			// Get the icon note resource
			final IFile iconFile = getProject().getFile(ICON_NOTE_PATH);
			if (iconFile.exists()) {
				// parse the icon note dxl
				Document document = parseIconNote(iconFile);
				
				// Locate the xots item, if any
				Element xotsElement = getXotsElement(document);
				
				if (xotsElement != null) {
					NodeList childNodes = xotsElement.getChildNodes();
					if (childNodes.getLength() == 1) {
						Element item = (Element) childNodes.item(0);
						if (item.getNodeName().equals("text")) {
							String xml = item.getTextContent();
							
							return XotsDatabaseConfig.fromXML(xml);
						}
					}
				}
			}
			else {
				logger.log(Level.SEVERE, "Error Icon Note does not exist");
			}
		}
		catch (Exception e) {
			logger.log(Level.SEVERE, "Error reading Xots configuration: ", e);
		}
		
		return null ;
	}

	@Override
	public void setXotsDatabaseConfig(XotsDatabaseConfig xotsDatabaseConfig) {
		try {

			// Get the icon note resource
			final IFile iconFile = getProject().getFile(ICON_NOTE_PATH);
			if (iconFile.exists()) {
				// parse the icon note dxl
				Document document = parseIconNote(iconFile);
				
				// Locate the xots item, if any				
				Element xotsElement = getXotsElement(document);

				if (xotsElement != null && xotsDatabaseConfig == null) {
					// no config but have item - > remove item
					document.getDocumentElement().removeChild(xotsElement);					
				}
				else if (xotsDatabaseConfig != null) {
					// serialize to xml
					String xml = xotsDatabaseConfig.toXML();
					
					// got some classes so replace the node
					Element itemElement = document.createElement("item");
					itemElement.setAttribute("name", XOTS_ITEM_NAME);
					
					Element textElement = document.createElement("text");
					CDATASection cdata = document.createCDATASection(xml);
					textElement.appendChild(cdata);
					//textElement.setTextContent(xml);
					itemElement.appendChild(textElement);
					
					if (xotsElement == null) {
						// add new xots item
						document.getDocumentElement().appendChild(itemElement);						
					}
					else {
						// replace existing xots item						
						document.getDocumentElement().replaceChild(itemElement, xotsElement);
					}
				}
				
				// serialize the xml and write it back to the icon note
				final String newXML = XMLUtils.serializeDOM(document);
				IWorkspaceRunnable runnable = new IWorkspaceRunnable() {					
					@Override
					public void run(IProgressMonitor arg0) throws CoreException {
						try {
							iconFile.setContents(new ByteArrayInputStream(newXML.getBytes()), true, true, null);	
						}
						catch (Exception e) {
							logger.log(Level.SEVERE, "Error saving icon note.", e);
						}
					}
				};
				runnable.run(null);
			}
			else {
				logger.log(Level.SEVERE, "Error Icon Note does not exist");
			}
		}
		catch (Exception e) {
			logger.log(Level.SEVERE, "Error updating Icon Note: ", e);
		}
		
	}


	@Override
	public IFile getFile() {
		return getProject().getFile(ICON_NOTE_PATH);
	}
}
