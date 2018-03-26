package io.blesmol.emf.cdo.provider;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.eclipse.net4j.Net4jUtil;
import org.eclipse.net4j.jvm.JVMUtil;
import org.eclipse.net4j.tcp.TCPUtil;
import org.eclipse.net4j.tcp.ssl.SSLUtil;
import org.eclipse.net4j.util.container.ContainerUtil;
import org.eclipse.net4j.util.container.IManagedContainer;
import org.junit.Test;

import io.blesmol.emf.cdo.api.CdoApi;

public class DelegatedAcceptorProviderTest {

	private CdoApi.IAcceptor config(String type, String description) {
		CdoApi.IAcceptor config = mock(CdoApi.IAcceptor.class);
		when(config.emf_cdo_acceptor_type()).thenReturn(type);
		when(config.emf_cdo_acceptor_description()).thenReturn(description);
		return config;
	}

	@Test
	public void shouldCreateJvmAcceptor() {
		final IManagedContainer container = ContainerUtil.createContainer();
		Net4jUtil.prepareContainer(container);
		JVMUtil.prepareContainer(container);
		container.activate();

		final DelegatedAcceptorProvider acceptor = new DelegatedAcceptorProvider();
		acceptor.setContainer(container);

		final CdoApi.IAcceptor config = config("jvm", getClass().getName());
		acceptor.activate(config, Collections.emptyMap());
		assertFalse(acceptor.isClosed());
		acceptor.deactivate(config);
		assertTrue(acceptor.isClosed());
		container.deactivate();
	}

	@Test
	public void shouldCreateTcpAcceptor() {
		final IManagedContainer container = ContainerUtil.createContainer();
		Net4jUtil.prepareContainer(container);
		TCPUtil.prepareContainer(container);
		container.activate();

		final DelegatedAcceptorProvider acceptor = new DelegatedAcceptorProvider();
		acceptor.setContainer(container);

		final CdoApi.IAcceptor config = config("tcp", "127.0.0.1:5432");
		acceptor.activate(config, Collections.emptyMap());
		assertFalse(acceptor.isClosed());
		acceptor.deactivate(config);
		assertTrue(acceptor.isClosed());
		container.deactivate();
	}

	@Test
	public void shouldCreateSslAcceptor() {
		final IManagedContainer container = ContainerUtil.createContainer();
		Net4jUtil.prepareContainer(container);
		SSLUtil.prepareContainer(container);
		container.activate();

		final DelegatedAcceptorProvider acceptor = new DelegatedAcceptorProvider();
		acceptor.setContainer(container);

		final CdoApi.IAcceptor config = config("ssl", "127.0.0.1:5433");
		acceptor.activate(config, Collections.emptyMap());
		assertFalse(acceptor.isClosed());
		acceptor.deactivate(config);
		assertTrue(acceptor.isClosed());
		container.deactivate();
	}

}
