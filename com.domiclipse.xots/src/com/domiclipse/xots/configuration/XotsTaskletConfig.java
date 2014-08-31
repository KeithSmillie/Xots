package com.domiclipse.xots.configuration;

import java.util.logging.Logger;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.domiclipse.xots.model.IXotsTaskletConfig;

@XmlRootElement
public class XotsTaskletConfig extends AbstractModelObject  implements IXotsTaskletConfig {
	private static final Logger logger = Logger.getLogger(XotsTaskletConfig.class.getName());

	boolean enabled = false ;
	String className = "" ;

	@Override
	public void setEnabled(boolean enabled) {
		firePropertyChange("enabled", this.enabled, this.enabled = enabled);
	}

	@XmlAttribute
	@Override
	public boolean isEnabled() {
		return this.enabled ;
	}

	@Override
	public void setClassName(String className) {
		firePropertyChange("className", this.className, this.className = className);		
	}

	@XmlAttribute
	@Override
	public String getClassName() {
		return this.className;
	}
}
