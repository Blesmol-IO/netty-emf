package io.blesmol.netty.notify.impl;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.notify.impl.DelegatingNotifyingListImpl;
import org.eclipse.emf.common.util.EList;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

// Make delegating?
public class NotifyingServerBootstrapImpl extends DelegatingServerBootstrapImpl implements Notifier {

	public static final int SERVER_BOOTSTRAP__CHANNEL_FUTURES = 0;

	protected final List<ChannelFuture> backedChannelFutures = new CopyOnWriteArrayList<>();
	protected final Notifier delegatedNotifier = new AtomicNotifierImpl();

	protected final ChannelFutureListener addListener = new ChannelFutureListener() {
		@Override
		public void operationComplete(final ChannelFuture outerFuture) throws Exception {
			if (outerFuture.isSuccess()) {
				channelFutures.addUnique(outerFuture);
				outerFuture.channel().closeFuture().addListener(new ChannelFutureListener() {
					@Override
					public void operationComplete(ChannelFuture innerFuture) throws Exception {
						channelFutures.basicRemove(outerFuture, null).dispatch();
					}
				});
			}
		}
	};

	protected final DelegatingNotifyingListImpl<ChannelFuture> channelFutures = new DelegatingNotifyingListImpl<ChannelFuture>() {
		private static final long serialVersionUID = 1L;

		@Override
		protected List<ChannelFuture> delegateList() {
			return backedChannelFutures;
		}

		@Override
		public int getFeatureID() {
			return SERVER_BOOTSTRAP__CHANNEL_FUTURES;
		};

		@Override
		protected int getFeatureID(java.lang.Class<?> expectedClass) {
			return SERVER_BOOTSTRAP__CHANNEL_FUTURES;
		};

		@Override
		public Object getNotifier() {
			return NotifyingServerBootstrapImpl.this;
		};

		@Override
		protected boolean isNotificationRequired() {
			return !NotifyingServerBootstrapImpl.this.eAdapters().isEmpty();
		};
	};

	protected void activate() {
		super.activate();
	}

	protected void deactivate() {
		super.deactivate();
		channelFutures.clear();
	}

	@Override
	public EList<Adapter> eAdapters() {
		return delegatedNotifier.eAdapters();
	}

	@Override
	public boolean eDeliver() {
		return delegatedNotifier.eDeliver();
	}

	@Override
	public void eSetDeliver(boolean deliver) {
		delegatedNotifier.eSetDeliver(deliver);
	}

	@Override
	public void eNotify(Notification notification) {
		delegatedNotifier.eNotify(notification);
	}

	@Override
	public ChannelFuture register() {
		final ChannelFuture future = delegatingBootstrap.register();
		future.addListener(addListener);
		return future;
	}

}
