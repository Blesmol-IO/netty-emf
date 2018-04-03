package io.blesmol.netty.notify.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;

public class NotifyingChannelPipelineTest {

	private static final String FIRST = "first";
	private static final String SECOND = "second";
	private static final String THIRD = "third";
	private static final String FORTH = "forth";

	protected static final Logger logger = LoggerFactory.getLogger(NotifyingChannelPipeline.class);

	private Notification verifyNotification(ChannelPipeline pipeline, Adapter adapter) {
		final ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
		verify(adapter).notifyChanged(notificationCaptor.capture());
		final Notification actualNotification = notificationCaptor.getValue();
		assertEquals(pipeline, actualNotification.getNotifier());
		assertEquals(NotifyingChannelPipeline.CHANNEL_PIPELINE__NAMES, actualNotification.getFeatureID(null));
		return actualNotification;
	}

	private List<Notification> verifyNotifications(ChannelPipeline pipeline, Adapter adapter) {
		final ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
		verify(adapter, times(2)).notifyChanged(notificationCaptor.capture());
		final List<Notification> results = notificationCaptor.getAllValues();
		results.forEach(n -> {
			assertEquals(pipeline, n.getNotifier());
			assertEquals(NotifyingChannelPipeline.CHANNEL_PIPELINE__NAMES, n.getFeatureID(null));
		});
		return results;
	}

	private void whenNames(ChannelPipeline delegatingPipeline, List<String> names) {
		when(delegatingPipeline.names()).then(new Answer<List<String>>() {
			@Override
			public List<String> answer(InvocationOnMock arg0) throws Throwable {
				logger.info("Returning names {}", names);
				return new ArrayList<>(names);
			}
		});
	}

	@Test
	public void shouldNotifyAddAfter() {
		final NotifyingChannelPipeline pipeline = new NotifyingChannelPipeline();

		// Mocks
		final Adapter mockedAdapter = mock(Adapter.class);
		pipeline.delegatedNotifier.eAdapters().add(mockedAdapter);

		final List<String> names = new ArrayList<>();
		names.add(FIRST);
		names.add(SECOND);
		final ChannelPipeline delegatingPipeline = mock(ChannelPipeline.class);
		whenNames(delegatingPipeline, names);
		when(delegatingPipeline.addAfter(any(), any(), any(), any())).then(new Answer<ChannelPipeline>() {
			@Override
			public ChannelPipeline answer(InvocationOnMock arg0) throws Throwable {
				names.add(THIRD);
				return delegatingPipeline;
			}
		});
		pipeline.delegatingPipeline = delegatingPipeline;

		// Execute
		pipeline.addAfter(SECOND, THIRD, null);

		final Notification actualNotification = verifyNotification(pipeline, mockedAdapter);
		// Verify event type, new / old values, position
		assertEquals(Notification.ADD, actualNotification.getEventType());
		assertEquals(THIRD, actualNotification.getNewValue());
		assertNull(actualNotification.getOldValue());
		assertEquals(2, actualNotification.getPosition());
	}

	@Test
	public void shouldNotifyAddBefore() {
		final NotifyingChannelPipeline pipeline = new NotifyingChannelPipeline();

		// Mocks
		final Adapter mockedAdapter = mock(Adapter.class);
		pipeline.delegatedNotifier.eAdapters().add(mockedAdapter);

		final List<String> names = new ArrayList<>();
		names.add(FIRST);
		names.add(THIRD);
		final ChannelPipeline delegatingPipeline = mock(ChannelPipeline.class);
		whenNames(delegatingPipeline, names);
		when(delegatingPipeline.addBefore(any(), any(), any(), any())).then(new Answer<ChannelPipeline>() {
			@Override
			public ChannelPipeline answer(InvocationOnMock arg0) throws Throwable {
				names.add(1, SECOND);
				return delegatingPipeline;
			}
		});
		pipeline.delegatingPipeline = delegatingPipeline;

		// Execute
		pipeline.addBefore(null, THIRD, SECOND, null);

		final Notification actualNotification = verifyNotification(pipeline, mockedAdapter);
		// Verify event type, new / old values, position
		assertEquals(Notification.ADD, actualNotification.getEventType());
		assertEquals(SECOND, actualNotification.getNewValue());
		assertNull(actualNotification.getOldValue());
		assertEquals(1, actualNotification.getPosition());
	}

	@Test
	public void shouldNotifyAddFirst() {
		final NotifyingChannelPipeline pipeline = new NotifyingChannelPipeline();

		// Mocks
		final Adapter mockedAdapter = mock(Adapter.class);
		pipeline.delegatedNotifier.eAdapters().add(mockedAdapter);

		final List<String> names = new ArrayList<>();
		names.add(SECOND);
		names.add(THIRD);
		final ChannelPipeline delegatingPipeline = mock(ChannelPipeline.class);
		whenNames(delegatingPipeline, names);
		when(delegatingPipeline.addFirst(any(), any(), any())).then(new Answer<ChannelPipeline>() {
			@Override
			public ChannelPipeline answer(InvocationOnMock arg0) throws Throwable {
				names.add(0, FIRST);
				return delegatingPipeline;
			}
		});
		pipeline.delegatingPipeline = delegatingPipeline;

		// Execute
		pipeline.addFirst(null, FIRST, null);

		final Notification actualNotification = verifyNotification(pipeline, mockedAdapter);
		// Verify event type, new / old values, position
		assertEquals(Notification.ADD, actualNotification.getEventType());
		assertEquals(FIRST, actualNotification.getNewValue());
		assertNull(actualNotification.getOldValue());
		assertEquals(0, actualNotification.getPosition());
	}

	@Test
	public void shouldNotifyAddLast() {
		final NotifyingChannelPipeline pipeline = new NotifyingChannelPipeline();

		// Mocks
		final Adapter mockedAdapter = mock(Adapter.class);
		pipeline.delegatedNotifier.eAdapters().add(mockedAdapter);

		final List<String> names = new ArrayList<>();
		names.add(FIRST);
		names.add(SECOND);
		final ChannelPipeline delegatingPipeline = mock(ChannelPipeline.class);
		whenNames(delegatingPipeline, names);
		when(delegatingPipeline.addLast(any(), any(), any())).then(new Answer<ChannelPipeline>() {
			@Override
			public ChannelPipeline answer(InvocationOnMock arg0) throws Throwable {
				names.add(THIRD);
				return delegatingPipeline;
			}
		});

		pipeline.delegatingPipeline = delegatingPipeline;

		// Execute
		pipeline.addLast(null, THIRD, null);

		final Notification actualNotification = verifyNotification(pipeline, mockedAdapter);
		// Verify event type, new / old values, position
		assertEquals(Notification.ADD, actualNotification.getEventType());
		assertEquals(THIRD, actualNotification.getNewValue());
		assertNull(actualNotification.getOldValue());
		assertEquals(2, actualNotification.getPosition());
	}

	/*
	 * There's only one remove implementation, so only one test
	 */
	@Test
	public void shouldNotifyRemove() {
		final NotifyingChannelPipeline pipeline = new NotifyingChannelPipeline();

		// Mocks
		final Adapter mockedAdapter = mock(Adapter.class);
		pipeline.delegatedNotifier.eAdapters().add(mockedAdapter);

		final List<String> names = new ArrayList<>();
		names.add(FIRST);
		names.add(SECOND);
		names.add(THIRD);
		final ChannelPipeline delegatingPipeline = mock(ChannelPipeline.class);
		whenNames(delegatingPipeline, names);
		when(delegatingPipeline.remove(anyString())).then(new Answer<ChannelHandler>() {
			@Override
			public ChannelHandler answer(InvocationOnMock arg0) throws Throwable {
				names.remove(FIRST);
				return null;
			}
		});
		pipeline.delegatingPipeline = delegatingPipeline;

		// Execute
		pipeline.remove(FIRST);

		final Notification actualNotification = verifyNotification(pipeline, mockedAdapter);
		// Verify event type, new / old values, position
		assertEquals(Notification.REMOVE, actualNotification.getEventType());
		assertEquals(null, actualNotification.getNewValue());
		assertEquals(FIRST, actualNotification.getOldValue());
		assertEquals(0, actualNotification.getPosition());
	}

	/*
	 * There's only one replace implementation, so only one test
	 */
	@Test
	public void shouldNotifyReplace() {
		final NotifyingChannelPipeline pipeline = new NotifyingChannelPipeline();

		// Mocks
		final Adapter mockedAdapter = mock(Adapter.class);
		pipeline.delegatedNotifier.eAdapters().add(mockedAdapter);

		final List<String> names = new ArrayList<>();
		names.add(FIRST);
		names.add(FORTH);
		names.add(THIRD);
		final ChannelPipeline delegatingPipeline = mock(ChannelPipeline.class);
		whenNames(delegatingPipeline, names);
		when(delegatingPipeline.replace(anyString(), anyString(), any())).then(new Answer<ChannelHandler>() {
			@Override
			public ChannelHandler answer(InvocationOnMock arg0) throws Throwable {
				names.remove(1);
				names.add(1, SECOND);
				return null;
			}
		});
		pipeline.delegatingPipeline = delegatingPipeline;

		// Execute
		pipeline.replace(FORTH, SECOND, null);

		final List<Notification> actualNotifications = verifyNotifications(pipeline, mockedAdapter);
		// Verify event type, new / old values, position
		Notification actualRemove = actualNotifications.get(0);
		assertEquals(Notification.REMOVE, actualRemove.getEventType());
		assertEquals(null, actualRemove.getNewValue());
		assertEquals(FORTH, actualRemove.getOldValue());
		assertEquals(1, actualRemove.getPosition());
		
		Notification actualAdd = actualNotifications.get(1);
		assertEquals(Notification.ADD, actualAdd.getEventType());
		assertEquals(SECOND, actualAdd.getNewValue());
		assertEquals(null, actualAdd.getOldValue());
		assertEquals(1, actualAdd.getPosition());
	}

}
