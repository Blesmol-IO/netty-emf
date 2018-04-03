package io.blesmol.netty.emf.handler;

import static org.junit.Assert.*;

import java.nio.charset.StandardCharsets;

import org.junit.Test;

import io.blesmol.netty.model.buffer.EByteBufHolder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;

public class EmfByteBufDecoderTest {

	@Test
	public void shouldDecode() {
		final byte[] expected = new String("This is a test.").getBytes(StandardCharsets.UTF_8);
		ByteBuf in = Unpooled.copiedBuffer(expected);
		in.retain();

		EmbeddedChannel channel = new EmbeddedChannel(new EmfByteBufDecoder());
		channel.writeInbound(in);
		channel.finish();
		assertEquals(0, in.refCnt());
		
		Object inbound = channel.readInbound();
		
		assertTrue(inbound instanceof EByteBufHolder);
		EByteBufHolder holder = (EByteBufHolder) inbound;

		final byte[] actual = holder.getContents();
		assertArrayEquals(expected, actual);

	}

}
