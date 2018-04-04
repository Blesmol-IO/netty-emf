package io.blesmol.netty.notify.api;

import io.netty.channel.ChannelFuture;

public interface NotifyingServerBootstrap extends NotifyingAbstractBootstrap {

	ChannelFuture register();
	
}
