package io.blesmol.netty.adapter.transport;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Optional;

import io.blesmol.netty.model.transport.Netty;
import io.blesmol.netty.model.transport.TransportFactory;
import io.blesmol.netty.notify.api.NotifyingAbstractBootstrap;
import io.blesmol.netty.notify.api.NotifyingServerBootstrap;
import io.netty.bootstrap.AbstractBootstrapConfig;

public class NotifyingServerBootstrapAdapterImpl extends NotifyingAbstractBootstrapAdapterImpl {

	@Override
	public boolean isAdapterForType(Object type) {
		return (type instanceof NotifyingServerBootstrap) ? true : super.isAdapterForType(type);
	}

	@Override
	protected io.blesmol.netty.model.transport.ServerBootstrap getAbstractBootstrapModel(boolean create,
			NotifyingAbstractBootstrap notifier, AbstractBootstrapConfig<?, ?> config) {
		io.blesmol.netty.model.transport.ServerBootstrap result = null;
		final String id = notifier.getId();
		final Netty netty = getOrCreateNetty();
		final Optional<io.blesmol.netty.model.transport.ServerBootstrap> optional = netty.getBootstraps().stream()
				.filter(ab -> ab.getId().equals(id))
				.filter(ab -> ab instanceof io.blesmol.netty.model.transport.ServerBootstrap)
				.map(ab -> (io.blesmol.netty.model.transport.ServerBootstrap) ab).findFirst();
		if (optional.isPresent()) {
			result = optional.get();
		} else if (create) {
			final SocketAddress addr = config.localAddress();
			result = TransportFactory.eINSTANCE.createServerBootstrap();
			result.setId(id);
			if (addr instanceof InetSocketAddress) {
				final InetSocketAddress inetAddr = (InetSocketAddress) addr;
				result.setBindHost(inetAddr.getHostName());
				result.setBindPort(inetAddr.getPort());
			}
			netty.getBootstraps().add(result);
		}
		return result;
	}

}
