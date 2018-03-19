package io.blesmol.netty.emf.handler;

import org.eclipse.emf.ecore.EObject;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.ReferenceCountUtil;

public class EmfInboundPersister extends ChannelInboundHandlerAdapter {

	protected EmfPersister persister;

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		
		if (msg instanceof ByteBuf) {
			// Decode		
			EmbeddedChannel embedded = new EmbeddedChannel(ctx.channel().id(), ctx.channel().metadata().hasDisconnect(),
	                ctx.channel().config(), new EmfByteBufDecoder());
			ReferenceCountUtil.retain(msg);
			embedded.writeInbound(msg);
			final Object inbound = embedded.readInbound();

			if (inbound instanceof EObject) {
				persister.eSet((EObject)inbound);
			}
		}
		// pass through
		super.channelRead(ctx, msg);
	}
}
