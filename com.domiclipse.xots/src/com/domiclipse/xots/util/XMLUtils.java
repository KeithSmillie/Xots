package com.domiclipse.xots.util;

import java.io.StringWriter;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

public class XMLUtils {
	private static final Logger logger = Logger.getLogger(XMLUtils.class.getName());
	
	public static String serializeDOM(Document doc)    {
	    DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
	    LSSerializer lsSerializer = domImplementation.createLSSerializer();
	    
	    LSOutput output = domImplementation.createLSOutput();
	    output.setEncoding("UTF-8");
	    StringWriter stringOut = new StringWriter(); 
	    output.setCharacterStream(stringOut);
	    lsSerializer.write(doc, output);
	    
	    return stringOut.toString();
	}
}
