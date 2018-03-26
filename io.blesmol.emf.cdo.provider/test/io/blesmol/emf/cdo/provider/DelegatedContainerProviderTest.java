package io.blesmol.emf.cdo.provider;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Test;
import org.osgi.service.log.LogService;

import io.blesmol.emf.cdo.api.CdoApi;

public class DelegatedContainerProviderTest {

	private CdoApi.IManagedContainer config(String type) {
		CdoApi.IManagedContainer config = mock(CdoApi.IManagedContainer.class);
		when(config.emf_cdo_managedcontainer_type()).thenReturn(type);
		return config;
	}
	
	private LogService logger = mock(LogService.class);

	@Test
	public void shouldCreateJvmContainer() {
		DelegatedContainerProvider container = new DelegatedContainerProvider();
		CdoApi.IManagedContainer config = config("jvm");
		container.logger = logger;
		container.activate(config, Collections.emptyMap());
		assertTrue(container.isActive());
		container.deactivate(config);
	}

	@Test
	public void shouldCreateTcpContainer() {
		DelegatedContainerProvider container = new DelegatedContainerProvider();
		CdoApi.IManagedContainer config = config("tcp");
		container.logger = logger;
		container.activate(config, Collections.emptyMap());
		assertTrue(container.isActive());
		container.deactivate(config);
	}

	@Test
	public void shouldCreateSslContainer() {
		DelegatedContainerProvider container = new DelegatedContainerProvider();
		CdoApi.IManagedContainer config = config("ssl");
		container.logger = logger;
		container.activate(config, Collections.emptyMap());
		assertTrue(container.isActive());
		container.deactivate(config);
	}

}
