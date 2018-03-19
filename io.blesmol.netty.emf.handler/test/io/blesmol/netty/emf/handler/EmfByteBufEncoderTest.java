package io.blesmol.netty.emf.handler;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;

import org.junit.Test;

import io.blesmol.netty.emf.model.EByteBufHolder;
import io.blesmol.netty.emf.model.ModelFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;

public class EmfByteBufEncoderTest {

	@Test
	public void shouldEncode() {
		final byte[] expected = new String("This is a test.").getBytes(StandardCharsets.UTF_8);
		
		EByteBufHolder holder = ModelFactory.eINSTANCE.createEByteBufHolder();
		holder.setContents(expected);
		
		EmbeddedChannel channel = new EmbeddedChannel(new EmfByteBufEncoder());
		channel.writeOutbound(holder);
		channel.finish();
		Object outbound = channel.readOutbound();

		assertTrue(outbound instanceof ByteBuf);
		ByteBuf out = (ByteBuf)outbound;
		
		int outLength = out.readableBytes();
		final byte [] actual = new byte[outLength];
		out.getBytes(out.readerIndex(), actual);
		assertArrayEquals(expected, actual);
		
	}

}
