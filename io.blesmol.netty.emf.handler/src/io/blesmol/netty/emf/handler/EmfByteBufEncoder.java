package io.blesmol.netty.emf.handler;

import io.blesmol.netty.model.buffer.EByteBufHolder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class EmfByteBufEncoder extends MessageToByteEncoder<EByteBufHolder> {

	@Override
	protected void encode(ChannelHandlerContext ctx, EByteBufHolder msg, ByteBuf out) throws Exception {
		out.writeBytes(msg.getContents());
	}

}
