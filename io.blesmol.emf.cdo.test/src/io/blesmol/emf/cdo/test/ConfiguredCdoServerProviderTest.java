package io.blesmol.emf.cdo.test;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.service.cm.Configuration;

import io.blesmol.emf.cdo.api.CdoServer;

/**
 * 
 * The tests below use a thread sleep as a synchronizer to wait for dependent
 * bundles to load after the CdoServer is REGISTERED. However, this only occurs
 * once per OSGi container instantiation versus once per test. If assertion
 * fails occur at the end with a message like so:
 * 
 * java.lang.AssertionError: expected:<8> but was:<4>
 * 
 * Consider either adding logic to wait for this one-time event (which might be
 * Equinox-specific) or increase the sleep time.
 */
@RunWith(MockitoJUnitRunner.class)
public class ConfiguredCdoServerProviderTest extends AbstractTest {

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@After
	public void after() {
		super.after();
	}

	@Test
	public void shouldConfigureSingleJvmServer() throws Exception {

		final String repoName = "jvmTest1";
		final String description = getClass().getName();
		final String type = "jvm";

		// Prep & configure
		final CountDownLatch latch = new CountDownLatch(1);
		final Map<String, Object> properties = prep(tempFolder, CdoServer.class.getName(), latch, repoName, description, type, /* client */ false,
				/* ssl */ false);
		final Configuration cdoServer = configureForServer(properties);

		// Verify
		verify(latch, cdoServer, CDO_PROVIDER_BUNDLE, CDO_SERVER_TYPE);
	}

	@Test
	public void shouldConfigureSingleTcpServer() throws Exception {

		final String repoName = "tcpTest";
		final String description = "127.0.0.1:55443";
		final String type = "tcp";

		// Prep & configure
		final CountDownLatch latch = new CountDownLatch(1);
		final Map<String, Object> properties = prep(tempFolder, CdoServer.class.getName(), latch, repoName, description, type, /* client */ false,
				/* ssl */ false);
		final Configuration cdoServer = configureForServer(properties);

		// Verify
		verify(latch, cdoServer, CDO_PROVIDER_BUNDLE, CDO_SERVER_TYPE);
	}

	@Test
	public void shouldConfigureSingleSslServer() throws Exception {

		final String repoName = "sslTest";
		final String description = "127.0.0.1:55444";
		final String type = "ssl";

		// Prep & configure
		final CountDownLatch latch = new CountDownLatch(1);
		final Map<String, Object> properties = prep(tempFolder, CdoServer.class.getName(), latch, repoName, description, type, /* client */ false,
				/* ssl */ true);
		final Configuration cdoServer = configureForServer(properties);

		// Verify
		verify(latch, cdoServer, CDO_PROVIDER_BUNDLE, CDO_SERVER_TYPE);
	}

}
