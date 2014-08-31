package com.domiclipse.xots.configuration;

import java.io.ByteArrayInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.domiclipse.xots.model.IXotsConfigStore;
import com.domiclipse.xots.util.XMLUtils;

/**
 * A store for the Xots database configuration that stores the data in the database Icon Note.
 * The config is stored in the $XOTS item. Each tasklet is in the form:
 * 	<classname>;<enabled>
 * 
 * eg 
 * com.acme.foo;true
 * com.acme.bar;false
 * 
 * @author Keith Smillie
 *
 */
public class IconNoteXotsConfigStore2 implements IXotsConfigStore {
	private static final Logger logger = Logger.getLogger(IconNoteXotsConfigStore2.class.getName());
	
	/** Location of the Icon Note */
	public static final String ICON_NOTE_PATH = "Resources/IconNote";
	
	/** Name of the item on the icon note holding xots config */
	public static final String XOTS_ITEM_NAME = "$Xots";	
	
	private final IProject project;

	public IconNoteXotsConfigStore2(IProject project) {
		this.project = project;
	}
	
	@Override
	public IFile getFile() {
		return getProject().getFile(ICON_NOTE_PATH);
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
	
	/**
	 * Get the XML element representing the $Xots item.
	 * 
	 * @param document
	 * @return
	 */
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
		XotsDatabaseConfig databaseConfig = null ;
		try {
			// Get the icon note resource
			final IFile iconFile = getProject().getFile(ICON_NOTE_PATH);
			if (iconFile.exists()) {
				// parse the icon note dxl
				Document document = parseIconNote(iconFile);
				
				// Locate the xots item, if any
				Element xotsElement = getXotsElement(document);
				
				if (xotsElement != null) {
					databaseConfig = new XotsDatabaseConfig() ;
					
					NodeList childNodes = xotsElement.getChildNodes();
					if (childNodes.getLength() == 1) {
						Element item = (Element) childNodes.item(0);
						if (item.getNodeName().equals("text")) {
							// A text node so only a single classname
							String value = item.getTextContent();
							String[] split = value.split(";");
							if (split.length == 2) {
								String className = split[0];
								boolean enabled = Boolean.valueOf(split[1]);
								XotsTaskletConfig taskletConfig = new XotsTaskletConfig();
								taskletConfig.setClassName(className);
								taskletConfig.setEnabled(enabled);
								databaseConfig.getTaskletConfigs().add(taskletConfig);								
							}
							else {
								logger.severe("Invalid value in Xots item: " + value);
							}
						}
						else if (item.getNodeName().equals("textlist")) {
							// A textlist node so only multiple classnames
							NodeList textNodes = item.getChildNodes();
							for (int x = 0 ; x < textNodes.getLength(); x++) {
								Element textNode = (Element) textNodes.item(x);
								
								String value = textNode.getTextContent();
								String[] split = value.split(";");
								if (split.length == 2) {
									String className = split[0];
									boolean enabled = Boolean.valueOf(split[1]);
									XotsTaskletConfig taskletConfig = new XotsTaskletConfig();
									taskletConfig.setClassName(className);
									taskletConfig.setEnabled(enabled);
									databaseConfig.getTaskletConfigs().add(taskletConfig);								
								}								
							}
						}						
						else {
							throw new Exception("Invalid XML node in XOTS item: " + item.getNodeName()) ;
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
		
		return databaseConfig ;
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
					// got some classes so replace the node
					Element itemElement = document.createElement("item");
					itemElement.setAttribute("name", XOTS_ITEM_NAME);
					
					Element textlistElement = document.createElement("textlist");
					itemElement.appendChild(textlistElement);
					
					for (XotsTaskletConfig taskletConfig : xotsDatabaseConfig.getTaskletConfigs()) {
	
						String value = taskletConfig.getClassName() + ";" + taskletConfig.isEnabled();
						
						Element textElement = document.createElement("text");
						textElement.setTextContent(value);
							
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
}
