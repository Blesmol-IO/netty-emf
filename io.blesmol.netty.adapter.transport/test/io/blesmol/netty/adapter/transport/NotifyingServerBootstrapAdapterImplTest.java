package io.blesmol.netty.adapter.transport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.InetSocketAddress;
import java.util.UUID;

import org.junit.Test;

import io.blesmol.netty.model.transport.AbstractBootstrap;
import io.blesmol.netty.model.transport.Netty;
import io.blesmol.netty.model.transport.ServerBootstrap;
import io.blesmol.netty.model.transport.TransportFactory;
import io.blesmol.netty.notify.api.NotifyingAbstractBootstrap;

public class NotifyingServerBootstrapAdapterImplTest {

	@Test
	public void shouldCreateAbstractBootstrapModel() {
		final Netty expectedNetty = TransportFactory.eINSTANCE.createNetty();
		NotifyingServerBootstrapAdapterImpl impl = new NotifyingServerBootstrapAdapterImpl() {
			@Override
			protected Netty getOrCreateNetty() {
				return expectedNetty;
			}
		};

		NotifyingAbstractBootstrap notifier = mock(NotifyingAbstractBootstrap.class);
		final String expectedId = UUID.randomUUID().toString();
		when(notifier.getId()).thenReturn(expectedId);
		InetSocketAddress expectedAddress = InetSocketAddress.createUnresolved(
				NotifyingAbstractBootstrapAdapterImplTest.LOCALHOST,
				NotifyingAbstractBootstrapAdapterImplTest.LOCALPORT);
		// No good way to mock below via netty due to a lot of finals
		final io.netty.bootstrap.ServerBootstrap serverBootstrap = new io.netty.bootstrap.ServerBootstrap();
		serverBootstrap.localAddress(expectedAddress);

		// Execute
		int priorSize = expectedNetty.getBootstraps().size();
		impl.getAbstractBootstrapModel(true, notifier, serverBootstrap.config());

		// Verify
		assertEquals(0, priorSize);
		assertEquals(1, expectedNetty.getBootstraps().size());
		final AbstractBootstrap abstractBootstrap = expectedNetty.getBootstraps().get(0);
		assertTrue(abstractBootstrap instanceof ServerBootstrap);
		ServerBootstrap modelBootstrap = (ServerBootstrap) abstractBootstrap;
		assertEquals(NotifyingAbstractBootstrapAdapterImplTest.LOCALHOST, modelBootstrap.getBindHost());
		assertEquals(NotifyingAbstractBootstrapAdapterImplTest.LOCALPORT, modelBootstrap.getBindPort());
		assertEquals(expectedId, modelBootstrap.getId());
	}

	@Test
	public void shouldGetAbstractBootstrapModel() {
		final String expectedId = UUID.randomUUID().toString();
		final Netty expectedNetty = TransportFactory.eINSTANCE.createNetty();
		NotifyingServerBootstrapAdapterImpl impl = new NotifyingServerBootstrapAdapterImpl() {
			@Override
			protected Netty getOrCreateNetty() {
				return expectedNetty;
			}
		};

		ServerBootstrap expectedBootstrap = TransportFactory.eINSTANCE.createServerBootstrap();
		expectedBootstrap.setBindHost(NotifyingAbstractBootstrapAdapterImplTest.LOCALHOST);
		expectedBootstrap.setBindPort(NotifyingAbstractBootstrapAdapterImplTest.LOCALPORT);
		expectedBootstrap.setId(expectedId);
		expectedNetty.getBootstraps().add(expectedBootstrap);

		NotifyingAbstractBootstrap notifier = mock(NotifyingAbstractBootstrap.class);
		when(notifier.getId()).thenReturn(expectedId);

		// Execute
		int priorSize = expectedNetty.getBootstraps().size();

		// Verify
		assertEquals(1, priorSize);
		final AbstractBootstrap actualBootstrap = impl.getAbstractBootstrapModel(false, notifier, null);
		assertEquals(expectedBootstrap, actualBootstrap);

	}

}
