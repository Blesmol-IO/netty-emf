package io.blesmol.netty.adapter.transport;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Optional;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.blesmol.netty.model.transport.Channel;
import io.blesmol.netty.model.transport.Netty;
import io.blesmol.netty.model.transport.TransportFactory;
import io.blesmol.netty.notify.api.NotifyingAbstractBootstrap;
import io.netty.bootstrap.AbstractBootstrap;
import io.netty.bootstrap.AbstractBootstrapConfig;
import io.netty.channel.ChannelFuture;

public abstract class NotifyingAbstractBootstrapAdapterImpl extends AdapterImpl {

	protected static final Logger logger = LoggerFactory.getLogger(NotifyingAbstractBootstrapAdapterImpl.class);

	protected volatile Resource resource;

	protected void activate(Resource resource) {
		this.resource = resource;
	}

	protected void deactivate(Resource resource) throws Exception {
		if (target != null) {
			target.eAdapters().remove(this);
		}
		if (this.resource.isModified()) {
			this.resource.save(null);
		}
		this.resource = null;
	}

	protected Netty getOrCreateNetty() {
		Netty netty = (Netty) resource.getContents().get(0);
		if (netty == null) {
			netty = TransportFactory.eINSTANCE.createNetty();
			resource.getContents().add(0, netty);
		}
		return netty;
	}

	@Override
	public void notifyChanged(Notification msg) {
		if (msg.getNotifier() instanceof NotifyingAbstractBootstrap && msg.getNotifier() instanceof AbstractBootstrap) {
			final NotifyingAbstractBootstrap notifier = (NotifyingAbstractBootstrap) msg.getNotifier();
			final AbstractBootstrapConfig<?, ?> config = ((AbstractBootstrap<?, ?>) msg.getNotifier()).config();
			final io.blesmol.netty.model.transport.AbstractBootstrap modelBootstrap = getAbstractBootstrapModel(true,
					notifier, config);

			if (modelBootstrap == null) {
				logger.warn("Subclass did not create a model via notifacion {}", msg);
				return;
			}

			switch (msg.getEventType()) {
			case Notification.ADD:
				addChannelFuture(modelBootstrap, (ChannelFuture) msg.getNewValue());
				break;
			case Notification.REMOVE:
				removeChannelFuture(modelBootstrap, (ChannelFuture) msg.getOldValue());
				break;
			default:
				logger.warn("Unhandled notification event type for notification {}", msg);
			}
			return;
		}
		super.notifyChanged(msg);
	}

	protected Channel createChannel(io.blesmol.netty.model.transport.AbstractBootstrap model,
			ChannelFuture channelFuture) {
		final Channel result = TransportFactory.eINSTANCE.createChannel();
		final SocketAddress localAddress = channelFuture.channel().localAddress();
		final SocketAddress remoteAddress = channelFuture.channel().remoteAddress();
		InetSocketAddress inetAddress;
		if (localAddress instanceof InetSocketAddress) {
			inetAddress = (InetSocketAddress) localAddress;
			result.setLocalHost(inetAddress.getHostName());
			result.setLocalPort(inetAddress.getPort());
		}
		if (remoteAddress instanceof InetSocketAddress) {
			inetAddress = (InetSocketAddress) remoteAddress;
			result.setRemoteHost(inetAddress.getHostName());
			result.setRemotePort(inetAddress.getPort());
		}
		result.setId(channelFuture.channel().id().asLongText());
		result.setBootstrap(model);
		return result;
	}

	protected void addChannelFuture(io.blesmol.netty.model.transport.AbstractBootstrap model,
			ChannelFuture channelFuture) {
		model.getChannels().add(createChannel(model, channelFuture));
	}

	protected void removeChannelFuture(io.blesmol.netty.model.transport.AbstractBootstrap model,
			ChannelFuture channelFuture) {
		final String id = channelFuture.channel().id().asLongText();
		Optional<Channel> maybeChannel = model.getChannels().stream().filter(c -> c.getId().equals(id)).findFirst();
		maybeChannel.ifPresent(c -> {
			model.getChannels().remove(c);
		});
	}

	protected abstract io.blesmol.netty.model.transport.AbstractBootstrap getAbstractBootstrapModel(boolean create,
			NotifyingAbstractBootstrap notifier, AbstractBootstrapConfig<?, ?> config);

}
