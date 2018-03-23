package io.blesmol.emf.cdo.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.security.KeyStore;
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
import io.blesmol.emf.cdo.api.CdoViewProvider;
import io.blesmol.testutil.ServiceHelper;

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
public class ConfiguredCdoViewProviderProviderTest {

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	private final BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();
	private CdoOsgiTestUtils testUtils = new CdoOsgiTestUtils();
	private ServiceHelper serviceHelper = new ServiceHelper();

	/**
	 * @see org.eclipse.net4j.internal.tcp.ssl.SSLProperties.TRUST_PATH
	 */
	private static final String TRUST_PATH = "org.eclipse.net4j.tcp.ssl.trust";

	/**
	 * @see org.eclipse.net4j.internal.tcp.ssl.SSLProperties.PASS_PHRASE
	 */
	private static final String PASS_PHRASE = "org.eclipse.net4j.tcp.ssl.passphrase";

	/**
	 * @see org.eclipse.net4j.internal.tcp.ssl.SSLProperties.KEY_PATH
	 */
	private static final String KEY_PATH = "org.eclipse.net4j.tcp.ssl.key";

	/**
	 * @see org.eclipse.net4j.internal.tcp.ssl.SSLProperties.CHECK_VALIDITY_CERTIFICATE
	 */
	private static final String CHECK_VALIDITY_CERTIFICATE = "check.validity.certificate";

	@After
	public void after() {
		serviceHelper.clear();
		// TODO: instead of just clearing, test if previously set; if so, retain value
		// and then reapply here
		System.clearProperty(TRUST_PATH);
		System.clearProperty(KEY_PATH);
		System.clearProperty(CHECK_VALIDITY_CERTIFICATE);
		System.clearProperty(PASS_PHRASE);
	}

	private Map<String, Object> prepViewProvider(CountDownLatch latch, String repoName, String description, String type,
			boolean client, boolean ssl) throws Exception {
		// Assumes H2 is being used for DB testing
		File repoFile = tempFolder.newFile(repoName + CdoOsgiTestUtils.H2_SUFFIX);

		// Synchronize to when the CdoViewProvider is registered
		final CdoOsgiTestUtils.LatchServiceListener listener;
		if (client) {
			listener = new CdoOsgiTestUtils.LatchServiceListener(latch, CdoViewProvider.class.getName());
		} else {
			listener = new CdoOsgiTestUtils.LatchServiceListener(latch, CdoServer.class.getName());
		}
		context.addServiceListener(listener);

		// Prep
		final Map<String, Object> properties = new HashMap<>();
		testUtils.putTargets(properties, repoName);
		testUtils.putMinimalProperties(properties, description, type, testUtils.cleanH2FileUrl(repoFile));

		// An SSL container needs to be prepared minimally with the following
		if (client && ssl) {
			final String password = "tooManySecrets";
			// And set CDO SSL properties (cleared in @After)
			System.setProperty(TRUST_PATH, createKeyStore("truststore", password));
			System.setProperty(KEY_PATH, createKeyStore("keystore", password));
			System.setProperty(CHECK_VALIDITY_CERTIFICATE, "false");
			System.setProperty(PASS_PHRASE, password);
		}
		return properties;
	}

	private String createKeyStore(String filename, String password) throws Exception {
		final KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		final File ksFile = tempFolder.newFile(filename);

		// create a new key store with a null input stream
		ks.load(null, password.toCharArray());
		// then save it
		try (FileOutputStream fos = new FileOutputStream(ksFile.getAbsolutePath())) {
			ks.store(fos, password.toCharArray());
		}
		return ksFile.toURI().toURL().toString();
	}

	private Configuration configureForViewProvider(Map<String, Object> properties) throws Exception {
		// Server & Client
		serviceHelper.createFactoryConfiguration(context, Optional.empty(), CdoApi.IDBAdapter.PID, properties);
		serviceHelper.createFactoryConfiguration(context, Optional.empty(), CdoApi.IDBConnectionProvider.PID,
				properties);
		serviceHelper.createFactoryConfiguration(context, Optional.empty(), CdoApi.IManagedContainer.PID, properties);

		// Server
		serviceHelper.createFactoryConfiguration(context, Optional.empty(), CdoApi.IAcceptor.PID, properties);
		serviceHelper.createFactoryConfiguration(context, Optional.empty(), CdoApi.CdoServer.PID, properties);

		// Client
		serviceHelper.createFactoryConfiguration(context, Optional.empty(), CdoApi.IConnector.PID, properties);
		return serviceHelper.createFactoryConfiguration(context, Optional.empty(), CdoApi.CdoViewProvider.PID,
				properties);
	}

	private void verify(CountDownLatch latch, Configuration configuration, String providerBundle, String providerType)
			throws Exception {
		assertTrue(latch.await(2000, TimeUnit.MILLISECONDS));
		Thread.sleep(2000);

		// Obtain the SCR and observe our CdoServer's configured component
		ServiceComponentRuntime scr = serviceHelper.getService(context, ServiceComponentRuntime.class, Optional.empty(),
				100);

		// Fragile! Hardcoded BSN and provider type
		Optional<Bundle> optBundle = Arrays.stream(context.getBundles())
				.filter(b -> b.getSymbolicName().contains(providerBundle)).findFirst();
		ComponentDescriptionDTO descriptionDto = scr.getComponentDescriptionDTO(optBundle.get(), providerType);
		assertNotNull(descriptionDto);

		Collection<ComponentConfigurationDTO> configurationDtos = scr.getComponentConfigurationDTOs(descriptionDto);
		// Filter on the service pid returned by the CdoServer configuration against
		// whatever is registered
		ComponentConfigurationDTO configurationDto = configurationDtos.stream()
				.filter(d -> ((String) d.properties.get(Constants.SERVICE_PID)).equals(configuration.getPid()))
				.findFirst().get();

		// Hack: if this assertion fails check that the BSN and provider type are
		// correct. If they are, consider increasing the sleep time above or figure out
		// a better synchronizer.
		assertEquals(ComponentConfigurationDTO.ACTIVE, configurationDto.state);
	}

	@Test
	public void shouldConfigureSingleJvmServer() throws Exception {

		final String repoName = "jvmTest1";
		final String description = getClass().getName();
		final String type = "jvm";

		// Prep & configure
		final CountDownLatch latch = new CountDownLatch(1);
		final Map<String, Object> properties = prepViewProvider(latch, repoName, description, type, true, false);
		final Configuration cdoViewProvider = configureForViewProvider(properties);

		// Verify
		verify(latch, cdoViewProvider, "io.blesmol.emf.cdo.provider",
				"io.blesmol.emf.cdo.provider.CdoViewProviderProvider");
	}

	@Test
	public void shouldConfigureSingleTcpServer() throws Exception {

		final String repoName = "tcpTest";
		final String description = "127.0.0.1:55443";
		final String type = "tcp";

		// Prep & configure
		final CountDownLatch latch = new CountDownLatch(1);
		final Map<String, Object> properties = prepViewProvider(latch, repoName, description, type, true, false);
		final Configuration cdoViewProvider = configureForViewProvider(properties);

		// Verify
		verify(latch, cdoViewProvider, "io.blesmol.emf.cdo.provider",
				"io.blesmol.emf.cdo.provider.CdoViewProviderProvider");
	}

	@Test
	public void shouldConfigureSingleSslServer() throws Exception {

		final String repoName = "sslTest";
		final String description = "127.0.0.1:55444";
		final String type = "ssl";

		// Prep & configure
		final CountDownLatch latch = new CountDownLatch(1);
		final Map<String, Object> properties = prepViewProvider(latch, repoName, description, type, true, true);
		final Configuration cdoViewProvider = configureForViewProvider(properties);

		// Verify
		verify(latch, cdoViewProvider, "io.blesmol.emf.cdo.provider",
				"io.blesmol.emf.cdo.provider.CdoViewProviderProvider");
	}

}
