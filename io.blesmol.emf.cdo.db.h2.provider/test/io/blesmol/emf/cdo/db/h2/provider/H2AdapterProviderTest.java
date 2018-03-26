package io.blesmol.emf.cdo.db.h2.provider;

import static org.junit.Assert.*;

import org.junit.Test;

public class H2AdapterProviderTest {

	@Test
	public void shouldCreateAdapter() {
		H2AdapterProvider h2 = new H2AdapterProvider();
		assertEquals("h2", h2.getName());
	}

}
