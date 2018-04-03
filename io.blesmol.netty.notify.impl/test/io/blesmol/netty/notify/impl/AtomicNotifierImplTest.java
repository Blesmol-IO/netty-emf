package io.blesmol.netty.notify.impl;

import static org.junit.Assert.*;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.Notifier;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.*;

public class AtomicNotifierImplTest {

	@Test
	public void shouldNotDeliver() {
		Notifier notifier = new AtomicNotifierImpl();
		notifier.eSetDeliver(false);
		assertFalse(notifier.eDeliver());
	}

	@Test
	public void shouldDeliver() {
		Notifier notifier = new AtomicNotifierImpl();
		notifier.eSetDeliver(true);
		final Adapter mockedAdapter = mock(Adapter.class);
		notifier.eAdapters().add(mockedAdapter);
		assertTrue(notifier.eDeliver());
	}

	@Test
	public void shouldNotify() {
		// Mock & Prep
		final Adapter mockedAdapter = mock(Adapter.class);
		final Notification mockedNotification = mock(Notification.class);
		Notifier notifier = new AtomicNotifierImpl();
		notifier.eSetDeliver(true);
		notifier.eAdapters().add(mockedAdapter);

		// Test
		notifier.eNotify(mockedNotification);

		// Verify
		final ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
		verify(mockedAdapter).notifyChanged(notificationCaptor.capture());
	}
}
