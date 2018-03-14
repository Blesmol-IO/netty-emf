package io.blesmol.netty.emf.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.junit.Test;

import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

public class EmfSaveHandlerITest {

	@Test
	public void shouldSetObject() throws Exception {

		final EcoreFactory ecoreFactory = EcoreFactory.eINSTANCE;
		final EClass receiverClass = ecoreFactory.createEClass();
		receiverClass.setName("Receiver");

		EAttribute thing = ecoreFactory.createEAttribute();
		final String thingName = "thing";
		thing.setName(thingName);
		thing.setEType(EcorePackage.Literals.EOBJECT);
		receiverClass.getEStructuralFeatures().add(thing);

		final EPackage receiverPackage = ecoreFactory.createEPackage();
		receiverPackage.setName("ReceiverPackage");
		receiverPackage.setNsPrefix("receiverPackage");
		receiverPackage.setNsURI("blesmol://test/receiver");
		receiverPackage.getEClassifiers().add(receiverClass);
		final EObject receiver = receiverPackage.getEFactoryInstance().create(receiverClass);

		final InboundEObjectPersister emfSaveHandler = new InboundEObjectPersister();
		emfSaveHandler.setEObject(receiver);
		emfSaveHandler.setEStructuralFeatureName(thingName);
		emfSaveHandler.setEventExecutorGroup(new DefaultEventExecutorGroup(1));

		final EClass messageClass = ecoreFactory.createEClass();
		messageClass.setName("Message");
		final EPackage messagePackage = ecoreFactory.createEPackage();
		messagePackage.setName("MessagePackage");
		messagePackage.setNsPrefix("messagePackage");
		messagePackage.setNsURI("blesmol://test/message");
		messagePackage.getEClassifiers().add(messageClass);
		final EObject expectedMessage = messagePackage.getEFactoryInstance().create(messageClass);

		EmbeddedChannel channel = new EmbeddedChannel(emfSaveHandler);
		channel.writeInbound(expectedMessage);
		channel.finish();

		// Check to see if the message was relayed correctly
		Object inboundMessage = channel.readInbound();
		assertNotNull(inboundMessage);
		assertEquals(expectedMessage, inboundMessage);

		// Hack. Consider decrementing some countdown latch, maybe via an adapter, when
		// the value is set
		Thread.sleep(100);
		Object receiverMessage = receiver.eGet(thing);
		assertNotNull(receiverMessage);
		assertEquals(expectedMessage, receiverMessage);

	}

}
