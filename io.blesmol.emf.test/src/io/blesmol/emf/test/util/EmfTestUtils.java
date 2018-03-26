package io.blesmol.emf.test.util;

import static org.junit.Assert.assertEquals;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
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
		return eAttribute(eClass, EcorePackage.Literals.EOBJECT, name);
	}

	public EAttribute eAttribute(EClass eClass, EClassifier eType, String name) {
		final EAttribute result = ecoreFactory.createEAttribute();
		result.setName(name);
		result.setEType(eType);
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

	public EObject eObject(String className, String attributeName, EClassifier attrType, String packageName, String nsPrefix, String nsUri) {
		EClass eClass = eClass(className);
		eAttribute(eClass, attrType, attributeName);
		EPackage ePackage = ePackage(eClass, packageName, nsPrefix, nsUri);
		return ePackage.getEFactoryInstance().create(eClass);
	}

	@Deprecated
	public void assertEObjects(String expectedClassName, String expectedAttrName, String expectedPackageName,
			String expectedNsPrefix, String expectedNsUri, EObject actualEObject) {
		assertEquals(expectedClassName, actualEObject.eClass().getName());
		assertEquals(expectedAttrName, actualEObject.eClass().getEStructuralFeatures().get(0).getName());
		assertEquals(expectedPackageName, actualEObject.eClass().getEPackage().getName());
		assertEquals(expectedNsPrefix, actualEObject.eClass().getEPackage().getNsPrefix());
		assertEquals(expectedNsUri, actualEObject.eClass().getEPackage().getNsURI().toString());
	}
	
	public void assertEObjects(EObject expectedEObject, EObject actualEObject) {
		assertEquals(expectedEObject.eClass().getName(), actualEObject.eClass().getName());
		assertEquals(expectedEObject.eClass().getEStructuralFeatures().get(0).getName(), actualEObject.eClass().getEStructuralFeatures().get(0).getName());
		assertEquals(expectedEObject.eClass().getEPackage().getName(), actualEObject.eClass().getEPackage().getName());
		assertEquals(expectedEObject.eClass().getEPackage().getNsPrefix(), actualEObject.eClass().getEPackage().getNsPrefix());
		assertEquals(expectedEObject.eClass().getEPackage().getNsURI().toString(), actualEObject.eClass().getEPackage().getNsURI().toString());
	}
}
