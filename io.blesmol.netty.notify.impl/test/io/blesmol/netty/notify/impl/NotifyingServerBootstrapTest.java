package io.blesmol.netty.notify.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

public class NotifyingServerBootstrapTest {

	protected static final Logger logger = LoggerFactory.getLogger(NotifyingServerBootstrapTest.class);

	@Test
	public void shouldNotifyOnAdd() {
		// Mock
		NotifyingServerBootstrapImpl bootstrap = new NotifyingServerBootstrapImpl();
		final Adapter mockedAdapter = mock(Adapter.class);
		bootstrap.eAdapters().add(mockedAdapter);

		final ChannelFuture mockedAddFuture = mock(ChannelFuture.class);
		when(mockedAddFuture.isSuccess()).thenReturn(true);
		when(mockedAddFuture.addListener(any())).then(new Answer<ChannelFuture>() {
			@Override
			public ChannelFuture answer(InvocationOnMock invocation) throws Throwable {
				ChannelFutureListener listener = (ChannelFutureListener)invocation.getArgument(0);
				listener.operationComplete(mockedAddFuture);
				logger.info("add future called operation complete");
				return mockedAddFuture;
			}
		});
		final Channel mockedChannel = mock(Channel.class);
		when(mockedAddFuture.channel()).thenReturn(mockedChannel);
		final ChannelFuture mockedCloseFuture = mock(ChannelFuture.class);
		when(mockedChannel.closeFuture()).thenReturn(mockedCloseFuture);
		when(mockedCloseFuture.addListener(any())).then(new Answer<ChannelFuture>() {
			@Override
			public ChannelFuture answer(InvocationOnMock invocation) throws Throwable {
				ChannelFutureListener listener = (ChannelFutureListener)invocation.getArgument(0);
				listener.operationComplete(mockedCloseFuture);
				logger.info("close future called operation complete");
				return mockedCloseFuture;
			}
		});

		final ServerBootstrap delegatingBootstrap = mock(ServerBootstrap.class);
		when(delegatingBootstrap.register()).thenReturn(mockedAddFuture);
		bootstrap.delegatingBootstrap = delegatingBootstrap;
		
		// Execute
		bootstrap.register();
		
		// Verify
		final ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
		verify(mockedAdapter, times(2)).notifyChanged(notificationCaptor.capture());
		Notification actualNotification = notificationCaptor.getValue();
		assertEquals(bootstrap, actualNotification.getNotifier());
		assertEquals(NotifyingServerBootstrapImpl.SERVER_BOOTSTRAP__CHANNEL_FUTURES, actualNotification.getFeatureID(null));


	}

}
