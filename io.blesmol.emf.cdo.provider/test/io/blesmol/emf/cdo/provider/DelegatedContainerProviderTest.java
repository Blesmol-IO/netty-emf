package io.blesmol.emf.cdo.provider;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import io.blesmol.emf.cdo.api.CdoApi;

public class DelegatedContainerProviderTest {

	private CdoApi.IManagedContainer config(String type) {
		CdoApi.IManagedContainer config = mock(CdoApi.IManagedContainer.class);
		when(config.type()).thenReturn(type);
		return config;
	}

	@Test
	public void shouldCreateJvmContainer() {
		DelegatedContainerProvider container = new DelegatedContainerProvider();
		CdoApi.IManagedContainer config = config("jvm");
		container.activate(config);
		assertTrue(container.isActive());
		container.deactivate(config);
	}

	@Test
	public void shouldCreateTcpContainer() {
		DelegatedContainerProvider container = new DelegatedContainerProvider();
		CdoApi.IManagedContainer config = config("tcp");
		container.activate(config);
		assertTrue(container.isActive());
		container.deactivate(config);
	}

	@Test
	public void shouldCreateSslContainer() {
		DelegatedContainerProvider container = new DelegatedContainerProvider();
		CdoApi.IManagedContainer config = config("ssl");
		container.activate(config);
		assertTrue(container.isActive());
		container.deactivate(config);
	}

}
