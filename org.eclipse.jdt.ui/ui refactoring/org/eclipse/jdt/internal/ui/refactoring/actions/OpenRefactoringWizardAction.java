package org.eclipse.jdt.internal.ui.refactoring.actions;
import java.util.Iterator;

import java.util.List;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.refactoring.Assert;
import org.eclipse.jdt.internal.core.refactoring.base.Refactoring;
import org.eclipse.jdt.internal.core.refactoring.tagging.IPreactivatedRefactoring;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.refactoring.RefactoringWizard;
import org.eclipse.jdt.internal.ui.refactoring.RefactoringWizardDialog;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.SelectionProviderAction;

public abstract class OpenRefactoringWizardAction extends SelectionProviderAction{
	
	private Class fActivationType;
	private Refactoring fRefactoring;
	
	public OpenRefactoringWizardAction(ISelectionProvider p, String label, Class activatedOnType) {
		super(p, label);
		Assert.isNotNull(activatedOnType);
		fActivationType= activatedOnType;
	}
	
	/**
	 *Set self's enablement based upon the currently selected resources
	 */
	public void selectionChanged(IStructuredSelection selection) {
		setEnabled(canExecute(selection.toList()));
	}
	
	protected boolean canExecute(List selection){
		for  (Iterator iter= selection.iterator(); iter.hasNext(); ) {
			Object obj= iter.next();
			if (!fActivationType.isInstance(obj) || !shouldAcceptElement(obj))
				return false;
		}
		return true;
	}
		
	/* non java-doc
	 * @see Action#run()
	 */
	public void run() {
		RefactoringWizard wizard= createWizard();
		wizard.init(fRefactoring);
		new RefactoringWizardDialog(JavaPlugin.getActiveWorkbenchShell(), wizard).open();
	}
		
	/**
	 * Creates a new instance of <code>Refactoring</code>.
	 * @param obj is guaranteed to be of the accepted type (passed in the constructor)
	 */
	protected abstract Refactoring createNewRefactoringInstance(Object obj) throws JavaModelException;	
	
	protected abstract RefactoringWizard createWizard();	
	
	/**
	 * @param obj is guaranteed to be of the accepted type (passed in the constructor)
	 * @see OpenWizardAction#shouldAcceptElement
	 */
	private final boolean shouldAcceptElement(Object obj) {
		try{
			fRefactoring= createNewRefactoringInstance(obj);
			return canActivateRefactoring();
		} catch (JavaModelException e){
				JavaPlugin.log(e.getStatus());
				return false;
		}	
	}
	
	private boolean canActivateRefactoring()  throws JavaModelException{
		//FIX ME: must have a better solution to this
		if (fRefactoring instanceof IPreactivatedRefactoring)
			return ((IPreactivatedRefactoring)fRefactoring).checkPreactivation().isOK();
		else	
			return fRefactoring.checkActivation(new NullProgressMonitor()).isOK();
	}
}

