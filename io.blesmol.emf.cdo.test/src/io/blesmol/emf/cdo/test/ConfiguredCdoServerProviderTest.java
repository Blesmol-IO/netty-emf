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
import org.junit.Before;
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
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.runtime.ServiceComponentRuntime;
import org.osgi.service.component.runtime.dto.ComponentConfigurationDTO;
import org.osgi.service.component.runtime.dto.ComponentDescriptionDTO;
import org.osgi.service.jdbc.DataSourceFactory;

import io.blesmol.emf.cdo.api.CdoApi;
import io.blesmol.emf.cdo.api.CdoServer;
import io.blesmol.testutil.ServiceHelper;

@RunWith(MockitoJUnitRunner.class)
public class ConfiguredCdoServerProviderTest {

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	private final BundleContext context = FrameworkUtil.getBundle(CdoRoundTripITest.class).getBundleContext();
	private ServiceHelper serviceHelper = new ServiceHelper();

	private String cleanH2FileUrl(File file) throws Exception {
		String results = file.toURI().toURL().toString();
		results = results.substring(0, results.indexOf(".mv.db"));
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
			// System.out.println(event.getSource());
			// Hacky way to verify; obtaining the service reference and then the
			// service causes unusual behavior, so don't do that :)
			if (event.getSource().toString().contains(sourceName)) {
				latch.countDown();
			}
		}

	}

	@Before
	public void before() throws Exception {

	}

	@After
	public void after() {
		serviceHelper.clear();
	}

	@Test
	public void shouldConfigureSingleJvmServer() throws Exception {

		String repoName = "jvmTest1";
		// H2
		File repoFile = tempFolder.newFile(repoName + ".mv.db");

		final CountDownLatch latch = new CountDownLatch(1);
		final LatchServiceListener listener = new LatchServiceListener(latch, CdoServer.class.getName());
		context.addServiceListener(listener);

		// Prep
		Map<String, Object> jvmProperties = new HashMap<>();
		putTargets(jvmProperties, repoName);
		putMinimalProperties(jvmProperties, getClass().getName(), "jvm", cleanH2FileUrl(repoFile));

		// Configure
		Configuration adapterConfig = serviceHelper.createFactoryConfiguration(context, Optional.empty(),
				CdoApi.IDBAdapter.PID, jvmProperties);
		Configuration connectionProvider = serviceHelper.createFactoryConfiguration(context, Optional.empty(),
				CdoApi.IDBConnectionProvider.PID, jvmProperties);
		Configuration containerConfig = serviceHelper.createFactoryConfiguration(context, Optional.empty(),
				CdoApi.IManagedContainer.PID, jvmProperties);
		Configuration acceptorConfig = serviceHelper.createFactoryConfiguration(context, Optional.empty(),
				CdoApi.IAcceptor.PID, jvmProperties);
		Configuration connectorConfig = serviceHelper.createFactoryConfiguration(context, Optional.empty(),
				CdoApi.IConnector.PID, jvmProperties);
		Configuration cdoServer = serviceHelper.createFactoryConfiguration(context, Optional.empty(),
				CdoApi.CdoServer.PID, jvmProperties);

		// Verify
		assertTrue(latch.await(3000, TimeUnit.MILLISECONDS));

		// Then wait a second for dependent bundles to load after the cdo server is registered
		Thread.sleep(1000);
		ServiceComponentRuntime scr = serviceHelper.getService(context, ServiceComponentRuntime.class, Optional.empty(),
				100);
		Optional<Bundle> optBundle = Arrays.stream(context.getBundles())
				.filter(b -> b.getSymbolicName().contains("io.blesmol.emf.cdo.provider")).findFirst();
		ComponentDescriptionDTO descriptionDto = scr.getComponentDescriptionDTO(optBundle.get(),
				"io.blesmol.emf.cdo.provider.CdoServerProvider");
		assertNotNull(descriptionDto);

		Collection<ComponentConfigurationDTO> configurationDtos = scr.getComponentConfigurationDTOs(descriptionDto);
		ComponentConfigurationDTO cdoServerDto = configurationDtos.stream()
				.filter(d -> ((String) d.properties.get(Constants.SERVICE_PID)).equals(cdoServer.getPid())).findFirst()
				.get();
		
		// Hack: if this assertion fails increase the sleep time above
		assertEquals(ComponentConfigurationDTO.ACTIVE, cdoServerDto.state);
	}

	@Test
	public void shouldConfigureSingleTcpServer() throws Exception {

		String repoName = "tcpTest1";
		// H2
		File repoFile = tempFolder.newFile(repoName + ".mv.db");

		// Prep
		Map<String, Object> tcpProperties = new HashMap<>();
		putTargets(tcpProperties, repoName);
		putMinimalProperties(tcpProperties, "127.0.0.1:55080", "tcp", cleanH2FileUrl(repoFile));
		final CountDownLatch latch = new CountDownLatch(1);
		final LatchServiceListener listener = new LatchServiceListener(latch, CdoServer.class.getName());
		context.addServiceListener(listener);

		// Configure
		serviceHelper.createFactoryConfiguration(context, Optional.empty(), CdoApi.IManagedContainer.PID,
				tcpProperties);
		serviceHelper.createFactoryConfiguration(context, Optional.empty(), CdoApi.IAcceptor.PID, tcpProperties);
		serviceHelper.createFactoryConfiguration(context, Optional.empty(), CdoApi.IConnector.PID, tcpProperties);
		serviceHelper.createFactoryConfiguration(context, Optional.empty(), CdoApi.IDBAdapter.PID, tcpProperties);
		String h2DataSourceFactoryFilter = "(osgi.jdbc.driver.class=org.h2.Driver)";
		DataSourceFactory h2DataSourceFactory = serviceHelper.getService(context, DataSourceFactory.class,
				Optional.of(h2DataSourceFactoryFilter), 100);
		assertNotNull(h2DataSourceFactory);
		serviceHelper.createFactoryConfiguration(context, Optional.empty(), CdoApi.CdoServer.PID, tcpProperties);

		// Verify
		assertTrue(latch.await(1000, TimeUnit.MILLISECONDS));
	}

	@Test
	public void shouldConfigureSingleSslServer() throws Exception {

		String repoName = "sslTest1";
		// H2
		File repoFile = tempFolder.newFile(repoName + ".mv.db");

		// Prep
		Map<String, Object> sslProperties = new HashMap<>();
		putTargets(sslProperties, "tcpTest1");
		putMinimalProperties(sslProperties, "127.0.0.1:55443", "ssl", cleanH2FileUrl(repoFile));
		final CountDownLatch latch = new CountDownLatch(1);
		final LatchServiceListener listener = new LatchServiceListener(latch, CdoServer.class.getName());
		context.addServiceListener(listener);

		// Configure
		serviceHelper.createFactoryConfiguration(context, Optional.empty(), CdoApi.IManagedContainer.PID,
				sslProperties);
		serviceHelper.createFactoryConfiguration(context, Optional.empty(), CdoApi.IAcceptor.PID, sslProperties);
		serviceHelper.createFactoryConfiguration(context, Optional.empty(), CdoApi.IConnector.PID, sslProperties);
		serviceHelper.createFactoryConfiguration(context, Optional.empty(), CdoApi.IDBAdapter.PID, sslProperties);
		String h2DataSourceFactoryFilter = "(osgi.jdbc.driver.class=org.h2.Driver)";
		DataSourceFactory h2DataSourceFactory = serviceHelper.getService(context, DataSourceFactory.class,
				Optional.of(h2DataSourceFactoryFilter), 100);
		assertNotNull(h2DataSourceFactory);
		serviceHelper.createFactoryConfiguration(context, Optional.empty(), CdoApi.CdoServer.PID, sslProperties);

		// Verify
		assertTrue(latch.await(1000, TimeUnit.MILLISECONDS));
	}

}
