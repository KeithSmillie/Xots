package com.domiclipse.xots.wizards;

import java.util.List;
import java.util.logging.Logger;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.wb.swt.ResourceManager;

import com.domiclipse.xots.configuration.XotsTaskletConfig;

public class XotsConfigPage extends WizardPage {
	private static final Logger logger = Logger.getLogger(XotsConfigPage.class.getName());
	
	private Table table;
	private CheckboxTableViewer tableViewer ;
	private TableViewerColumn typeViewerColumn;

	private final XotsWizardModel model;

	private TableViewerColumn checkedViewerColumn;
	
	/**
	 * Create the wizard page.
	 */
	public XotsConfigPage(XotsWizardModel model) {
		super("wizardPage");
		this.model = model;
		setTitle("Xots Configuration");
		setDescription("This list shows all the Xots compatible classes on the classpath of this database.\r\nSelect which ones to enable on the server.");
	}

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);

		setControl(container);
		container.setLayout(new GridLayout(1, false));
		
		ToolBar toolBar = new ToolBar(container, SWT.FLAT | SWT.RIGHT);
		toolBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		ToolItem tltmSelectAll = new ToolItem(toolBar, SWT.NONE);
		tltmSelectAll.setImage(ResourceManager.getPluginImage("com.domiclipse.xots", "icons/selectall.gif"));
		tltmSelectAll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				for (XotsTaskletConfig taskletConfig : getModel().getXotsConfig().getTaskletConfigs()) {
					taskletConfig.setEnabled(true);
					tableViewer.setChecked(taskletConfig, true);
				}
				tableViewer.refresh();
			}
		});
		tltmSelectAll.setText("Enable All");
		
		ToolItem tltmSelectNone = new ToolItem(toolBar, SWT.NONE);
		tltmSelectNone.setImage(ResourceManager.getPluginImage("com.domiclipse.xots", "icons/selectnone.gif"));
		tltmSelectNone.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				for (XotsTaskletConfig taskletConfig : getModel().getXotsConfig().getTaskletConfigs()) {
					taskletConfig.setEnabled(false);
					tableViewer.setChecked(taskletConfig, false);
				}
				
			}
		});
		tltmSelectNone.setText("Disable All");
		
		ToolItem tltmDelete = new ToolItem(toolBar, SWT.NONE);
		tltmDelete.setImage(ResourceManager.getPluginImage("com.domiclipse.xots", "icons/delete.gif"));
		tltmDelete.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
				XotsTaskletConfig taskletConfig = (XotsTaskletConfig) selection.getFirstElement();
				if (taskletConfig != null) {
					getModel().getXotsConfig().getTaskletConfigs().remove(taskletConfig);
					tableViewer.refresh();	
				}
			}
		});
		tltmDelete.setText("Remove");
		
		tableViewer = CheckboxTableViewer.newCheckList(container, SWT.BORDER | SWT.CHECK | SWT.FULL_SELECTION);
		ColumnViewerToolTipSupport.enableFor(tableViewer,ToolTip.NO_RECREATE);
		tableViewer.setUseHashlookup(true);
		table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		table.setSize(84, 84);
				
		checkedViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnType2 = checkedViewerColumn.getColumn();
		tblclmnType2.setWidth(300);
		tblclmnType2.setText("Xots Class");
		
		typeViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnType = typeViewerColumn.getColumn();
		tblclmnType.setWidth(300);
		tblclmnType.setText("Reource");
			
		/*testViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnTest = testViewerColumn.getColumn();
		tblclmnTest.setWidth(300);
		tblclmnTest.setText("Test");*/
						
		initBindings() ;
	}

	private void initBindings() {
		
		checkedViewerColumn.setLabelProvider(new XotsCellLabelProvider(XotsCellLabelProvider.Columns.COLUMN_CLASSNAME, getModel()));		
		typeViewerColumn.setLabelProvider(new XotsCellLabelProvider(XotsCellLabelProvider.Columns.COLUMN_RESOURCE, getModel()));
		//testViewerColumn.setLabelProvider(new XotsCellLabelProvider(XotsCellLabelProvider.Columns.COLUMN_TEST, getModel().getProject(), getModel().getTypes()));
		
		tableViewer.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {				
				XotsTaskletConfig taskletConfig = (XotsTaskletConfig) event.getElement();
				taskletConfig.setEnabled(event.getChecked());
				tableViewer.refresh(taskletConfig);
			}
		});
									
		tableViewer.setSorter(new ViewerSorter() {
			@Override
			public int compare(Viewer arg0, Object obj1, Object obj2) {
				String className1 = ((XotsTaskletConfig) obj1).getClassName() ;
				String className2 = ((XotsTaskletConfig) obj2).getClassName() ;
				
				return className1.compareTo(className2);
			}
			
		});
				
		//tableViewer.setLabelProvider(new XotsLabelProvider(getModel().getProject()));
		tableViewer.setContentProvider(new ArrayContentProvider());
		
		List<XotsTaskletConfig> taskletConfigs = getModel().getXotsConfig().getTaskletConfigs() ;
		tableViewer.setInput(taskletConfigs);
		 
		// Set the initial checked state		
		for (XotsTaskletConfig type : taskletConfigs) {
			if (type.isEnabled()) {
				tableViewer.setChecked(type, true);				
			}
		}
	}
	
	public XotsWizardModel getModel() {
		return model;
	}
}
