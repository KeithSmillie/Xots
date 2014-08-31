package com.domiclipse.xots.configuration;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.jdt.core.IType;

import com.domiclipse.xots.model.IXotsTaskletConfig;

@XmlRootElement(name = "xotsdatabaseconfig")
public class XotsDatabaseConfig extends AbstractModelObject {
	private static final Logger logger = Logger.getLogger(XotsDatabaseConfig.class.getName());

	List<XotsTaskletConfig> taskletConfigs ;
	

	public void setTaskletConfigs(List<XotsTaskletConfig> taskletConfigs) {
		this.taskletConfigs = taskletConfigs ;
	}


	@XmlElement( name = "tasklet")
	public List<XotsTaskletConfig> getTaskletConfigs() {
		if (taskletConfigs == null) {
			taskletConfigs = new ArrayList<XotsTaskletConfig>() ;
		}
		return taskletConfigs;
	}


	public IXotsTaskletConfig getTaskletConfig(String className) {
		for (IXotsTaskletConfig taskletConfig : getTaskletConfigs()) {
			if (taskletConfig.getClassName().equals(className)) {
				return taskletConfig ;
			}
		}
		
		return null;
	}
	
	/**
	 * Given a list of types, returns those which do not exist in the config.
	 * 
	 * @param types
	 * @return
	 */
	public List<IType> getUnknownTypes(List<IType> types) {
		List<IType> unknownTypes = new ArrayList<IType>() ;
		
		for (IType type : types) {
			String fqn = type.getFullyQualifiedName();
			if (getTaskletConfig(fqn) == null ) {
				// new type so add it to the config
				unknownTypes.add(type);
			}
		}
		
		return unknownTypes ;
	}
	
	public String toXML() throws JAXBException, UnsupportedEncodingException {
		JAXBContext context = JAXBContext.newInstance(XotsDatabaseConfig.class);
		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

		// Write to System.out
		ByteArrayOutputStream stream = new ByteArrayOutputStream() ;
		m.marshal(this, stream);
		
		return stream.toString("utf-8");
	}
	
	public static XotsDatabaseConfig fromXML(String xml) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(XotsDatabaseConfig.class);		
		Unmarshaller um = context.createUnmarshaller();
		StringReader reader = new StringReader(xml) ;
		return (XotsDatabaseConfig) um.unmarshal(reader);
	}
}
