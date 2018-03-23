package io.blesmol.emf.cdo.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.cm.Configuration;
import org.osgi.service.component.runtime.ServiceComponentRuntime;
import org.osgi.service.component.runtime.dto.ComponentConfigurationDTO;
import org.osgi.service.component.runtime.dto.ComponentDescriptionDTO;

import io.blesmol.emf.cdo.api.CdoApi;
import io.blesmol.emf.cdo.api.CdoServer;
import io.blesmol.testutil.ServiceHelper;

/**
 * 
 * The tests below use a thread sleep as a synchronizer to wait for dependent
 * bundles to load after the CdoServer is REGISTERED. However, this only occurs
 * once per OSGi container instantiation versus once per test. If assertion fails
 * occur at the end with a message like so:
 * 
 * java.lang.AssertionError: expected:<8> but was:<4>
 * 
 * Consider either adding logic to wait for this one-time event (which might be
 * Equinox-specific) or increase the sleep time.
 */
@RunWith(MockitoJUnitRunner.class)
public class ConfiguredCdoServerProviderTest {

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	private final BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();
	private CdoOsgiTestUtils testUtils = new CdoOsgiTestUtils();
	private ServiceHelper serviceHelper = new ServiceHelper();

	@After
	public void after() {
		serviceHelper.clear();
	}

	private Map<String, Object> prep(CountDownLatch latch, String repoName, String description, String type) throws Exception {		
		// Assumes H2 is being used for DB testing
		File repoFile = tempFolder.newFile(repoName + CdoOsgiTestUtils.H2_SUFFIX);

		// Synchronize to when the CdoServer is registered
		final CdoOsgiTestUtils.LatchServiceListener listener = new CdoOsgiTestUtils.LatchServiceListener(latch, CdoServer.class.getName());
		context.addServiceListener(listener);

		// Prep
		final Map<String, Object> properties = new HashMap<>();
		testUtils.putTargets(properties, repoName);
		testUtils.putMinimalProperties(properties, description, type, testUtils.cleanH2FileUrl(repoFile));
		return properties;
	}
	
	private Configuration configure(Map<String, Object> properties) throws Exception {
		serviceHelper.createFactoryConfiguration(context, Optional.empty(), CdoApi.IDBAdapter.PID, properties);
		serviceHelper.createFactoryConfiguration(context, Optional.empty(), CdoApi.IDBConnectionProvider.PID,
				properties);
		serviceHelper.createFactoryConfiguration(context, Optional.empty(), CdoApi.IManagedContainer.PID, properties);
		serviceHelper.createFactoryConfiguration(context, Optional.empty(), CdoApi.IAcceptor.PID, properties);
		// serviceHelper.createFactoryConfiguration(context, Optional.empty(),
		// CdoApi.IConnector.PID, properties);
		return serviceHelper.createFactoryConfiguration(context, Optional.empty(),
				CdoApi.CdoServer.PID, properties);
	}
	
	private void verify(CountDownLatch latch, Configuration configuration) throws Exception {
		assertTrue(latch.await(2000, TimeUnit.MILLISECONDS));
		Thread.sleep(1500);

		// Obtain the SCR and observe our CdoServer's configured component
		ServiceComponentRuntime scr = serviceHelper.getService(context, ServiceComponentRuntime.class, Optional.empty(),
				100);

		// Fragile! Hardcoded BSN and provider type
		Optional<Bundle> optBundle = Arrays.stream(context.getBundles())
				.filter(b -> b.getSymbolicName().contains("io.blesmol.emf.cdo.provider")).findFirst();
		ComponentDescriptionDTO descriptionDto = scr.getComponentDescriptionDTO(optBundle.get(),
				"io.blesmol.emf.cdo.provider.CdoServerProvider");
		assertNotNull(descriptionDto);

		Collection<ComponentConfigurationDTO> configurationDtos = scr.getComponentConfigurationDTOs(descriptionDto);
		// Filter on the service pid returned by the CdoServer configuration against
		// whatever is registered
		ComponentConfigurationDTO cdoServerDto = configurationDtos.stream()
				.filter(d -> ((String) d.properties.get(Constants.SERVICE_PID)).equals(configuration.getPid())).findFirst()
				.get();

		// Hack: if this assertion fails check that the BSN and provider type are
		// correct. If they are, consider increasing the sleep time above or figure out
		// a better synchronizer.
		assertEquals(ComponentConfigurationDTO.ACTIVE, cdoServerDto.state);
	}
	
	@Test
	public void shouldConfigureSingleJvmServer() throws Exception {

		final String repoName = "jvmTest1";
		final String description = getClass().getName();
		final String type = "jvm";

		// Prep & configure
		final CountDownLatch latch = new CountDownLatch(1);
		final Map<String, Object> properties = prep(latch, repoName, description, type);
		final Configuration cdoServer = configure(properties);

		// Verify
		verify(latch, cdoServer);
	}

	@Test
	public void shouldConfigureSingleTcpServer() throws Exception {

		final String repoName = "tcpTest";
		final String description = "127.0.0.1:55443";
		final String type = "tcp";

		// Prep & configure
		final CountDownLatch latch = new CountDownLatch(1);
		final Map<String, Object> properties = prep(latch, repoName, description, type);
		final Configuration cdoServer = configure(properties);

		// Verify
		verify(latch, cdoServer);
	}

	@Test
	public void shouldConfigureSingleSslServer() throws Exception {

		final String repoName = "sslTest";
		final String description = "127.0.0.1:55444";
		final String type = "ssl";

		// Prep & configure
		final CountDownLatch latch = new CountDownLatch(1);
		final Map<String, Object> properties = prep(latch, repoName, description, type);
		final Configuration cdoServer = configure(properties);

		// Verify
		verify(latch, cdoServer);
	}

}
