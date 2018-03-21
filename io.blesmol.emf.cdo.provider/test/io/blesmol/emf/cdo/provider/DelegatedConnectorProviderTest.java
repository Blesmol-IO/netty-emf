package io.blesmol.emf.cdo.provider;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.security.KeyStore;

import org.eclipse.net4j.Net4jUtil;
import org.eclipse.net4j.acceptor.IAcceptor;
import org.eclipse.net4j.internal.tcp.ssl.SSLProperties;
import org.eclipse.net4j.jvm.JVMUtil;
import org.eclipse.net4j.tcp.TCPUtil;
import org.eclipse.net4j.tcp.ssl.SSLUtil;
import org.eclipse.net4j.util.container.ContainerUtil;
import org.eclipse.net4j.util.container.IManagedContainer;
import org.eclipse.net4j.util.lifecycle.LifecycleUtil;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import io.blesmol.emf.cdo.api.CdoApi;

public class DelegatedConnectorProviderTest {

	// org.eclipse.net4j.internal.tcp.ssl.SSLProperties
	
	private static final String password = "tooManySecrets";

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	private CdoApi.IConnector config(String type, String description) {
		CdoApi.IConnector config = mock(CdoApi.IConnector.class);
		when(config.type()).thenReturn(type);
		when(config.description()).thenReturn(description);
		when(config.productGroup()).thenReturn("org.eclipse.net4j.connectors");
		return config;
	}

	@After
	public void after() {
		System.clearProperty(SSLProperties.TRUST_PATH);
		System.clearProperty(SSLProperties.PASS_PHRASE);
	}

	@Test
	public void shouldCreateJvmConnector() {
		CdoApi.IConnector config = config("jvm", getClass().getName());
		IManagedContainer container = ContainerUtil.createContainer();

		// A JVM container needs to be prepared minimally with the following
		Net4jUtil.prepareContainer(container); // Register Net4j factories
		JVMUtil.prepareContainer(container); // Register JVM factories

		// And activated
		container.activate(); // standalone

		// A JVM acceptor needs to be created before a connector
		IAcceptor acceptor = JVMUtil.getAcceptor(container, config.description());

		// Net4jUtil.prepareContainer(container); // Register Net4j factories
		// CDONet4jUtil.prepareContainer(container); // Register CDO client factories
		// container.activate(); // standalone
		DelegatedConnectorProvider connector = new DelegatedConnectorProvider();
		connector.setContainer(container);

		// Verify: throws exception if cannot get connector delegate
		connector.activate(config);
		connector.deactivate();
		LifecycleUtil.deactivate(acceptor);
	}

	@Test
	public void shouldCreateTcpConnector() {
		CdoApi.IConnector config = config("tcp", "127.0.0.1:5432");
		IManagedContainer container = ContainerUtil.createContainer();

		// A TCP container needs to be prepared minimally with the following
		Net4jUtil.prepareContainer(container); // Register Net4j factories
		TCPUtil.prepareContainer(container); // Register TCP factories

		// And activate
		container.activate(); // standalone

		// An acceptor needs to be available since the connector attempts to connect
		IAcceptor acceptor = TCPUtil.getAcceptor(container, config.description());

		DelegatedConnectorProvider connector = new DelegatedConnectorProvider();
		connector.setContainer(container);

		// Verify: throws exception if cannot get connector delegate
		connector.activate(config);
		connector.deactivate();
		LifecycleUtil.deactivate(acceptor);
	}

	@Test
	public void shouldCreateSslConnector() throws Exception {
		CdoApi.IConnector config = config("ssl", "127.0.0.1:5433");
		IManagedContainer container = ContainerUtil.createContainer();

		// An SSL container needs to be prepared minimally with the following
		Net4jUtil.prepareContainer(container); // Register Net4j factories
		SSLUtil.prepareContainer(container); // Register TCP factories
	    final KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
	    final File tempTrustFile = tempFolder.newFile("truststore");
	    // create a new key store with a null input stream
	    ks.load(null, password.toCharArray());
	    // then save it
	    try (FileOutputStream fos = new FileOutputStream(tempTrustFile.getAbsolutePath())) {
	        ks.store(fos, password.toCharArray());
	    }
	    // And set CDO SSL properties (cleared in @After)
		System.setProperty(SSLProperties.TRUST_PATH, tempTrustFile.toURI().toURL().toString());
		System.setProperty(SSLProperties.PASS_PHRASE, password);

		// And activate
		container.activate(); // standalone

		// An acceptor needs to be available since the connector attempts to connect
		IAcceptor acceptor = SSLUtil.getAcceptor(container, config.description());

		DelegatedConnectorProvider connector = new DelegatedConnectorProvider();
		connector.setContainer(container);

		// Verify: throws exception if cannot get connector delegate
		connector.activate(config);
		connector.deactivate();
		LifecycleUtil.deactivate(acceptor);
	}

}
