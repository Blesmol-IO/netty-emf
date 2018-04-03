package io.blesmol.netty.notify.impl;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.notify.impl.DelegatingNotifyingListImpl;
import org.eclipse.emf.common.util.EList;

// TODO: move somewhere else
public class AtomicNotifierImpl implements Notifier {

	protected volatile int deliver = TRUE;
	protected static final AtomicIntegerFieldUpdater<AtomicNotifierImpl> DELIVER_UPDATER = AtomicIntegerFieldUpdater
			.newUpdater(AtomicNotifierImpl.class, "deliver");

	protected static final int TRUE = 1;
	protected static final int FALSE = 0;

	protected final List<Adapter> backedAdapters = new CopyOnWriteArrayList<>();

	protected final EList<Adapter> eAdapters = new DelegatingNotifyingListImpl<Adapter>() {
		private static final long serialVersionUID = 1L;

		@Override
		protected List<Adapter> delegateList() {
			return backedAdapters;
		}
	};

	@Override
	public EList<Adapter> eAdapters() {
		return eAdapters;
	}

	@Override
	public void eNotify(Notification notification) {
		if (eDeliver()) {
			eAdapters.stream().forEach(a -> a.notifyChanged(notification));
		}
	}

	@Override
	public boolean eDeliver() {
		return DELIVER_UPDATER.get(this) == TRUE ? !eAdapters.isEmpty() : false;
	}

	@Override
	public void eSetDeliver(boolean deliver) {
		int update;
		int expect;
		do {
			expect = update = this.deliver;
			if (deliver) {
				update = TRUE;
			} else {
				update = FALSE;
			}
		} while (!DELIVER_UPDATER.compareAndSet(this, expect, update));

	}
}
