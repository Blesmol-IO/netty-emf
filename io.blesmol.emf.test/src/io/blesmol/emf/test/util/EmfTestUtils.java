package io.blesmol.emf.test.util;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;

public class EmfTestUtils {

	final EcoreFactory ecoreFactory = EcoreFactory.eINSTANCE;

	public EClass eClass(String name) {
		final EClass result = ecoreFactory.createEClass();
		result.setName(name);
		return result;
	}

	public EAttribute eAttribute(EClass eClass, String name) {
		final EAttribute result = ecoreFactory.createEAttribute();
		result.setName(name);
		result.setEType(EcorePackage.Literals.EOBJECT);
		eClass.getEStructuralFeatures().add(result);
		return result;
	}

	public EPackage ePackage(EClass eClass, String name, String nsPrefix, String nsUri) {
		final EPackage result = ecoreFactory.createEPackage();
		result.setName(name);
		result.setNsPrefix(nsPrefix);
		result.setNsURI(nsUri);
		result.getEClassifiers().add(eClass);
		return result;
	}

	public EObject eObject(String className, String attributeName, String packageName, String nsPrefix, String nsUri) {
		EClass eClass = eClass(className);
		eAttribute(eClass, attributeName);
		EPackage ePackage = ePackage(eClass, packageName, nsPrefix, nsUri);
		return ePackage.getEFactoryInstance().create(eClass);
	}
}
