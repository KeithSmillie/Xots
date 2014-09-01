package com.domiclipse.xots.preferences;

import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;
import com.domiclipse.xots.Activator;

/**
 * Preferences for the Xots builder.
 * 
 * @author Keith Smillie
 *
 */
public class XotsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public XotsPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Xots Builder Preferences");
	}
	
	public void createFieldEditors() {
		
		addField(
			new BooleanFieldEditor(
				XotsPreferenceConstants.ENABLE_NEW_XOTS,
				"Enable new Xots classes.",
				getFieldEditorParent()));
		
		addField(
				new BooleanFieldEditor(
					XotsPreferenceConstants.REMOVE_MISSING_XOTS,
					"Remove missing/deleted Xots classes.",
					getFieldEditorParent()));		
				
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
}