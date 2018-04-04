package io.blesmol.netty.adapter.transport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.InetSocketAddress;
import java.util.UUID;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.EList;
import org.junit.Test;

import io.blesmol.netty.model.transport.AbstractBootstrap;
import io.blesmol.netty.model.transport.TransportFactory;
import io.blesmol.netty.notify.api.NotifyingAbstractBootstrap;
import io.netty.bootstrap.AbstractBootstrapConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelId;

public class NotifyingAbstractBootstrapAdapterImplTest {

	public static class TestNotifyingServerBootstrap extends ServerBootstrap implements NotifyingAbstractBootstrap {
		@Override
		public EList<Adapter> eAdapters() {
			return null;
		}

		@Override
		public boolean eDeliver() {
			return false;
		}

		@Override
		public void eSetDeliver(boolean deliver) {
		}

		@Override
		public void eNotify(Notification notification) {
		}

		@Override
		public String getId() {
			return null;
		}
	}

	public static final String LOCALHOST = "localhost";
	public static final int LOCALPORT = 0;
	public static final String REMOTEHOST = "www.example.com";
	public static final int REMOTEPORT = 80;

	private ChannelFuture createMockFuture(String expectedChannelId, String localHost, int localPort, String remoteHost, int remotePort) {
		final ChannelId channelId = mock(ChannelId.class);
		when(channelId.asLongText()).thenReturn(expectedChannelId);
		final Channel channel = mock(Channel.class);
		when(channel.id()).thenReturn(channelId);
		final InetSocketAddress expectedLocalAddress = InetSocketAddress.createUnresolved(localHost, localPort);
		when(channel.localAddress()).thenReturn(expectedLocalAddress);
		final InetSocketAddress expectedRemoteAddress = InetSocketAddress.createUnresolved(remoteHost, remotePort);
		when(channel.remoteAddress()).thenReturn(expectedRemoteAddress);
		final ChannelFuture future = mock(ChannelFuture.class);
		when(future.channel()).thenReturn(channel);
		return future;

	}
	
	@Test
	public void shouldAddChannelViaNotification() {

		// Mocks and Prep
		final String expectedId = UUID.randomUUID().toString();
		final ChannelFuture future = createMockFuture(expectedId, LOCALHOST, LOCALPORT, REMOTEHOST, REMOTEPORT);

		final Notification notification = mock(Notification.class);
		when(notification.getEventType()).thenReturn(Notification.ADD);
		when(notification.getNewValue()).thenReturn(future);
		final TestNotifyingServerBootstrap notifyingBootstrap = new TestNotifyingServerBootstrap();
		when(notification.getNotifier()).thenReturn(notifyingBootstrap);

		final AbstractBootstrap expectedBootstrap = TransportFactory.eINSTANCE.createServerBootstrap();

		NotifyingAbstractBootstrapAdapterImpl testAdapter = new NotifyingAbstractBootstrapAdapterImpl() {
			@Override
			protected AbstractBootstrap getAbstractBootstrapModel(boolean create, NotifyingAbstractBootstrap notifier,
					AbstractBootstrapConfig<?, ?> config) {
				return expectedBootstrap;
			}

		};

		// Execute
		testAdapter.notifyChanged(notification);

		// Verify
		assertEquals(1, expectedBootstrap.getChannels().size());
		final io.blesmol.netty.model.transport.Channel actualChannel = expectedBootstrap.getChannels().get(0);
		assertNotNull(actualChannel);
		assertEquals(LOCALHOST, actualChannel.getLocalHost());
		assertEquals(LOCALPORT, actualChannel.getLocalPort());
		assertEquals(REMOTEHOST, actualChannel.getRemoteHost());
		assertEquals(REMOTEPORT, actualChannel.getRemotePort());
		assertEquals(expectedId, actualChannel.getId());
		assertEquals(expectedBootstrap, actualChannel.getBootstrap());
	}
	

	@Test
	public void shouldRemoveChannelViaNotification() {

		// Mocks and Prep
		final String expectedId = UUID.randomUUID().toString();
		final ChannelFuture future = createMockFuture(expectedId, LOCALHOST, LOCALPORT, REMOTEHOST, REMOTEPORT);

		final AbstractBootstrap expectedBootstrap = TransportFactory.eINSTANCE.createServerBootstrap();
		final io.blesmol.netty.model.transport.Channel fakeFirstChannel = TransportFactory.eINSTANCE.createChannel();
		final String fakeFirst = "fakefirst";
		fakeFirstChannel.setId(fakeFirst);
		expectedBootstrap.getChannels().add(fakeFirstChannel);
		final io.blesmol.netty.model.transport.Channel expectedChannel = TransportFactory.eINSTANCE.createChannel();
		expectedChannel.setId(expectedId);
		expectedBootstrap.getChannels().add(expectedChannel);
		final io.blesmol.netty.model.transport.Channel fakeLastChannel = TransportFactory.eINSTANCE.createChannel();
		final String fakeLast = "fakelast";
		fakeLastChannel.setId(fakeLast);
		expectedBootstrap.getChannels().add(fakeLastChannel);
		final Notification notification = mock(Notification.class);
		when(notification.getEventType()).thenReturn(Notification.REMOVE);
		when(notification.getOldValue()).thenReturn(future);
		final TestNotifyingServerBootstrap notifyingBootstrap = new TestNotifyingServerBootstrap();
		when(notification.getNotifier()).thenReturn(notifyingBootstrap);

		NotifyingAbstractBootstrapAdapterImpl testAdapter = new NotifyingAbstractBootstrapAdapterImpl() {
			@Override
			protected AbstractBootstrap getAbstractBootstrapModel(boolean create, NotifyingAbstractBootstrap notifier,
					AbstractBootstrapConfig<?, ?> config) {
				return expectedBootstrap;
			}

		};

		// Execute
		final int priorSize = expectedBootstrap.getChannels().size();
		testAdapter.notifyChanged(notification);

		// Verify
		assertEquals(3, priorSize);
		assertEquals(2, expectedBootstrap.getChannels().size());
		assertEquals(fakeFirst, expectedBootstrap.getChannels().get(0).getId());
		assertEquals(fakeLast, expectedBootstrap.getChannels().get(1).getId());
	}

}
