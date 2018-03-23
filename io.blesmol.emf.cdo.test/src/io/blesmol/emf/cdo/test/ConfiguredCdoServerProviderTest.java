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
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
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

	private static final String H2_SUFFIX = ".mv.db";

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	private final BundleContext context = FrameworkUtil.getBundle(CdoRoundTripITest.class).getBundleContext();
	private ServiceHelper serviceHelper = new ServiceHelper();

	private String cleanH2FileUrl(File file) throws Exception {
		String results = file.toURI().toURL().toString();
		results = results.substring(0, results.indexOf(H2_SUFFIX));
		return results;
	}

	private void putMinimalProperties(Map<String, Object> properties, String description, String type, String fileUrl) {
		properties.put("emf.cdo.connector.description", description);
		properties.put("emf.cdo.acceptor.description", description);
		properties.put("blesmol.cdoserver.reponame", description);
		properties.put("emf.cdo.managedcontainer.type", type);
		properties.put("emf.cdo.connector.type", type);
		properties.put("emf.cdo.acceptor.type", type);
		properties.put("emf.cdo.connectionprovider.url", fileUrl);
	}

	private void putTargets(Map<String, Object> properties, String targetValue) {
		final String targetKey = "blesmol.test";
		final String suffix = ".target";
		final String target = String.format("(%s=%s)", targetKey, targetValue);
		properties.put(targetKey, targetValue);
		properties.put(CdoApi.IDBConnectionProvider.Reference.DB_ADAPTER + suffix, target);
		properties.put(CdoApi.IAcceptor.Reference.MANAGED_CONTAINER + suffix, target);
		properties.put(CdoApi.IConnector.Reference.MANAGED_CONTAINER + suffix, target);
		properties.put(CdoApi.CdoServer.Reference.MANAGED_CONTAINER + suffix, target);
		properties.put(CdoApi.CdoServer.Reference.ACCEPTOR + suffix, target);
		properties.put(CdoApi.CdoServer.Reference.DB_ADAPTER + suffix, target);
		properties.put(CdoApi.CdoServer.Reference.DB_CONNECTION_PROVIDER + suffix, target);
	}

	public static class LatchServiceListener implements ServiceListener {

		private final CountDownLatch latch;
		private final String sourceName;

		public LatchServiceListener(CountDownLatch latch, String sourceName) {
			super();
			this.latch = latch;
			this.sourceName = sourceName;
		}

		@Override
		public void serviceChanged(ServiceEvent event) {
			if (event.getType() != ServiceEvent.REGISTERED)
				return;

			// Hack-y way, and probably implementation-specific way on filtering for source
			// references.
			// Note: obtaining the service reference and getting the service causes unusual
			// behavior, so don't do that :)
			if (event.getSource().toString().contains(sourceName)) {
				latch.countDown();
			}
		}

	}

	@After
	public void after() {
		serviceHelper.clear();
	}

	@Test
	public void shouldConfigureSingleJvmServer() throws Exception {

		final String repoName = "jvmTest1";
		final String description = getClass().getName();
		final String type = "jvm";
		// Assumes H2 is being used for DB testing
		File repoFile = tempFolder.newFile(repoName + H2_SUFFIX);

		// Synchronize to when the CdoServer is registered
		final CountDownLatch latch = new CountDownLatch(1);
		final LatchServiceListener listener = new LatchServiceListener(latch, CdoServer.class.getName());
		context.addServiceListener(listener);

		// Prep
		Map<String, Object> properties = new HashMap<>();
		putTargets(properties, repoName);
		putMinimalProperties(properties, description, type, cleanH2FileUrl(repoFile));

		// Configure, ignoring configurations except for CdoServer
		serviceHelper.createFactoryConfiguration(context, Optional.empty(), CdoApi.IDBAdapter.PID, properties);
		serviceHelper.createFactoryConfiguration(context, Optional.empty(), CdoApi.IDBConnectionProvider.PID,
				properties);
		serviceHelper.createFactoryConfiguration(context, Optional.empty(), CdoApi.IManagedContainer.PID, properties);
		serviceHelper.createFactoryConfiguration(context, Optional.empty(), CdoApi.IAcceptor.PID, properties);
		// serviceHelper.createFactoryConfiguration(context, Optional.empty(),
		// CdoApi.IConnector.PID, properties);
		final Configuration cdoServer = serviceHelper.createFactoryConfiguration(context, Optional.empty(),
				CdoApi.CdoServer.PID, properties);

		// Verify
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
				.filter(d -> ((String) d.properties.get(Constants.SERVICE_PID)).equals(cdoServer.getPid())).findFirst()
				.get();

		// Hack: if this assertion fails check that the BSN and provider type are
		// correct. If they are, consider increasing the sleep time above or figure out
		// a better synchronizer.
		assertEquals(ComponentConfigurationDTO.ACTIVE, cdoServerDto.state);
	}

	@Test
	public void shouldConfigureSingleTcpServer() throws Exception {

		final String repoName = "tcpTest";
		final String description = "127.0.0.1:55443";
		final String type = "tcp";
		File repoFile = tempFolder.newFile(repoName + H2_SUFFIX);

		// Synchronize to when the CdoServer is registered
		final CountDownLatch latch = new CountDownLatch(1);
		final LatchServiceListener listener = new LatchServiceListener(latch, CdoServer.class.getName());
		context.addServiceListener(listener);

		// Prep
		Map<String, Object> properties = new HashMap<>();
		putTargets(properties, description);
		putMinimalProperties(properties, description, type, cleanH2FileUrl(repoFile));

		// Configure, ignoring configurations except for CdoServer
		serviceHelper.createFactoryConfiguration(context, Optional.empty(), CdoApi.IDBAdapter.PID, properties);
		serviceHelper.createFactoryConfiguration(context, Optional.empty(), CdoApi.IDBConnectionProvider.PID,
				properties);
		serviceHelper.createFactoryConfiguration(context, Optional.empty(), CdoApi.IManagedContainer.PID, properties);
		serviceHelper.createFactoryConfiguration(context, Optional.empty(), CdoApi.IAcceptor.PID, properties);
		// serviceHelper.createFactoryConfiguration(context, Optional.empty(),
		// CdoApi.IConnector.PID, properties);
		final Configuration cdoServer = serviceHelper.createFactoryConfiguration(context, Optional.empty(),
				CdoApi.CdoServer.PID, properties);

		// Verify
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
				.filter(d -> ((String) d.properties.get(Constants.SERVICE_PID)).equals(cdoServer.getPid())).findFirst()
				.get();

		// Hack: if this assertion fails check that the BSN and provider type are
		// correct. If they are, consider increasing the sleep time above or figure out
		// a better synchronizer.
		assertEquals(ComponentConfigurationDTO.ACTIVE, cdoServerDto.state);
	}

	@Test
	public void shouldConfigureSingleSslServer() throws Exception {

		final String repoName = "sslTest";
		final String description = "127.0.0.1:55444";
		final String type = "ssl";
		File repoFile = tempFolder.newFile(repoName + H2_SUFFIX);

		// Synchronize to when the CdoServer is registered
		final CountDownLatch latch = new CountDownLatch(1);
		final LatchServiceListener listener = new LatchServiceListener(latch, CdoServer.class.getName());
		context.addServiceListener(listener);

		// Prep
		Map<String, Object> properties = new HashMap<>();
		putTargets(properties, description);
		putMinimalProperties(properties, description, type, cleanH2FileUrl(repoFile));

		// Configure, ignoring configurations except for CdoServer
		serviceHelper.createFactoryConfiguration(context, Optional.empty(), CdoApi.IDBAdapter.PID, properties);
		serviceHelper.createFactoryConfiguration(context, Optional.empty(), CdoApi.IDBConnectionProvider.PID,
				properties);
		serviceHelper.createFactoryConfiguration(context, Optional.empty(), CdoApi.IManagedContainer.PID, properties);
		serviceHelper.createFactoryConfiguration(context, Optional.empty(), CdoApi.IAcceptor.PID, properties);
		// serviceHelper.createFactoryConfiguration(context, Optional.empty(),
		// CdoApi.IConnector.PID, properties);
		final Configuration cdoServer = serviceHelper.createFactoryConfiguration(context, Optional.empty(),
				CdoApi.CdoServer.PID, properties);

		// Verify
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
				.filter(d -> ((String) d.properties.get(Constants.SERVICE_PID)).equals(cdoServer.getPid())).findFirst()
				.get();

		// Hack: if this assertion fails check that the BSN and provider type are
		// correct. If they are, consider increasing the sleep time above or figure out
		// a better synchronizer.
		assertEquals(ComponentConfigurationDTO.ACTIVE, cdoServerDto.state);
	}

}
