package io.blesmol.netty.emf.handler;

import java.util.List;

import io.blesmol.netty.emf.model.EByteBufHolder;
import io.blesmol.netty.emf.model.ModelFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class EmfByteBufDecoder extends ByteToMessageDecoder {

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		EByteBufHolder byteBufHolder = ModelFactory.eINSTANCE.createEByteBufHolder();
		int length = in.readableBytes();
		byte[] contents = new byte[length];
		in.readBytes(contents);
		in.release();
		byteBufHolder.setContents(contents);
		out.add(byteBufHolder);
	}

}
