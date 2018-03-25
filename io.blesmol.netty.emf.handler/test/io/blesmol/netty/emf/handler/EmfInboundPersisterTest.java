package io.blesmol.netty.emf.handler;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
import org.junit.Test;

import io.blesmol.emf.test.util.EmfTestUtils;
import io.blesmol.netty.emf.model.EByteBufHolder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

public class EmfInboundPersisterTest {

	EmfTestUtils emfTestUtils = new EmfTestUtils();

	@Test
	public void shouldDecodePersist() throws Exception {

		// EMF objects
		final String receiverAttrName = "thing";
		final EObject receiver = emfTestUtils.eObject("Receiver", receiverAttrName, EcorePackage.Literals.EOBJECT,
				"ReceiverPackage", "receiverPackage", "blesmol://test/receiver");
		final EStructuralFeature receiverAttr = receiver.eClass().getEStructuralFeatures().get(0);
		// final EObject persistedExpected = emfTestUtils.eObject("Message", "notUsed",
		// "MessagePackage", "messagePackage", "blesmol://test/message");

		// Create buffer
		final byte[] expectedMsg = new String("This is a test.").getBytes(StandardCharsets.UTF_8);
		ByteBuf byteBufIn = Unpooled.copiedBuffer(expectedMsg);

		// Retain twice so we can look at the message later
		byteBufIn.retain();
		byteBufIn.retain();

		// Configure persister
		final EmfInboundPersister persisterHandler = new EmfInboundPersister();
		persisterHandler.persister = new EmfPersister();
		persisterHandler.persister.eventExecutorGroup = new DefaultEventExecutorGroup(1);
		persisterHandler.persister.receiver = receiver;
		persisterHandler.persister.featureName = receiverAttrName;

		// Test
		EmbeddedChannel channel = new EmbeddedChannel(persisterHandler);
		channel.writeInbound(byteBufIn);
		channel.finish();

		// Verify that the handler passed through the message
		Object inbound = channel.readInbound();
		assertTrue(inbound instanceof ByteBuf);
		assertEquals(byteBufIn, inbound);
		byteBufIn.release();

		// Verify the handler persisted the message
		// Hack. Consider decrementing some countdown latch, maybe via an adapter, when
		// the value is set
		Thread.sleep(100);
		Object persisted = receiver.eGet(receiverAttr); // receiver.eGet(thing);
		// If the test fails here, consider incrementing the sleep above
		assertNotNull(persisted);

		assertTrue(persisted instanceof EByteBufHolder);
		EByteBufHolder holder = (EByteBufHolder) persisted;
		final byte[] actualMsg = holder.getContents();
		assertArrayEquals(expectedMsg, actualMsg);

	}

}
