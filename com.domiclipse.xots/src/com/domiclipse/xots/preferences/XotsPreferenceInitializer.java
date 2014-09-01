package com.domiclipse.xots.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.domiclipse.xots.Activator;

/**
 * Class used to initialize default preference values.
 */
public class XotsPreferenceInitializer extends AbstractPreferenceInitializer {

	public static final boolean ENABLE_NEW_XOTS_DEFAULT = true ;
	public static final boolean REMOVE_MISSING_XOTS_DEFAULT = true ;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(XotsPreferenceConstants.ENABLE_NEW_XOTS, ENABLE_NEW_XOTS_DEFAULT);
		store.setDefault(XotsPreferenceConstants.REMOVE_MISSING_XOTS, REMOVE_MISSING_XOTS_DEFAULT);		
	}

}
