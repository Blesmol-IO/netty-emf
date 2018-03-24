package io.blesmol.netty.emf.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.junit.Test;

import io.blesmol.emf.test.util.EmfTestUtils;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

public class EmfPersisterTest {
	
	EmfTestUtils emfTestUtils = new EmfTestUtils();

	@Test
	public void shouldSetObject() throws Exception {
		
		String receiverClassName = "Receiver";
		String receiverAttrName = "thing";
		String receiverPackage = "ReceiverPackage";
		String nsPrefix = "receiverPackage";
		String nsUri = "blesmol://test/receiver";

		final EClass receiverClass = emfTestUtils.eClass(receiverClassName);
		final EAttribute receiverAttr = emfTestUtils.eAttribute(receiverClass, receiverAttrName);
		final EPackage receiverPkg = emfTestUtils.ePackage(receiverClass, receiverPackage, nsPrefix, nsUri);
		final EObject receiver = receiverPkg.getEFactoryInstance().create(receiverClass);

		final EmfPersister persister = new EmfPersister();
		persister.receiver = receiver;
		persister.featureName = receiverAttrName; // thingName;
		persister.eventExecutorGroup = new DefaultEventExecutorGroup(1);
		
		String msgClassName = "Message";
		String msgAttrName = "notused";
		String msgPackage = "MessagePackage";
		String msgPrefix = "messagePackage";
		String msgUri = "blesmol://test/message";

		final EObject expected = emfTestUtils.eObject(msgClassName, msgAttrName, msgPackage, msgPrefix, msgUri);
		persister.eSet(expected);		

		// Hack. Consider decrementing some countdown latch, maybe via an adapter, when
		// the value is set
		Thread.sleep(100);
		Object actual = receiver.eGet(receiverAttr); // receiver.eGet(thing);
		// If the test fails here, consider incrementing the sleep above
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

}
