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

import org.junit.rules.TemporaryFolder;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.cm.Configuration;
import org.osgi.service.component.runtime.ServiceComponentRuntime;
import org.osgi.service.component.runtime.dto.ComponentConfigurationDTO;
import org.osgi.service.component.runtime.dto.ComponentDescriptionDTO;

import io.blesmol.emf.api.EmfApi;
import io.blesmol.emf.cdo.api.CdoApi;
import io.blesmol.testutil.ServiceHelper;

public abstract class AbstractTest {

	protected static final String CDO_PROVIDER_BUNDLE = "io.blesmol.emf.cdo.provider";

	protected static final String VIEW_PROVIDER_TYPE = "io.blesmol.emf.cdo.provider.CdoViewProviderProvider";

	protected static final String CDO_SERVER_TYPE = "io.blesmol.emf.cdo.provider.CdoServerProvider";

	// Move to EMF test util?
	protected static final String EMF_PROVIDER_BUNDLE = "io.blesmol.emf.provider";
	protected static final String RESOURCE_FACTORY_TYPE = "io.blesmol.emf.provider.ResourceFactoryRegistryProvider";

	/**
	 * @see org.eclipse.net4j.internal.tcp.ssl.SSLProperties.TRUST_PATH
	 */
	protected static final String TRUST_PATH = "org.eclipse.net4j.tcp.ssl.trust";

	/**
	 * @see org.eclipse.net4j.internal.tcp.ssl.SSLProperties.PASS_PHRASE
	 */
	protected static final String PASS_PHRASE = "org.eclipse.net4j.tcp.ssl.passphrase";

	/**
	 * @see org.eclipse.net4j.internal.tcp.ssl.SSLProperties.KEY_PATH
	 */
	protected static final String KEY_PATH = "org.eclipse.net4j.tcp.ssl.key";

	/**
	 * @see org.eclipse.net4j.internal.tcp.ssl.SSLProperties.CHECK_VALIDITY_CERTIFICATE
	 */

	protected static final String CHECK_VALIDITY_CERTIFICATE = "check.validity.certificate";
	protected final BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();
	protected final CdoOsgiTestUtils testUtils = new CdoOsgiTestUtils();
	protected final ServiceHelper serviceHelper = new ServiceHelper();

	public void after() {
		serviceHelper.clear();
		// TODO: instead of just clearing, test if previously set; if so, retain value
		// and then reapply here
		System.clearProperty(TRUST_PATH);
		System.clearProperty(KEY_PATH);
		System.clearProperty(CHECK_VALIDITY_CERTIFICATE);
		System.clearProperty(PASS_PHRASE);
	}

	protected String createKeyStore(TemporaryFolder tempFolder, String filename, String password) throws Exception {
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

	// TODO: move to an Emf class?
	protected Configuration configureForResourceFactory(Map<String, Object> properties) throws Exception {
		serviceHelper.createFactoryConfiguration(context, Optional.empty(), EmfApi.Resource_Factory.PID, properties);
		serviceHelper.createFactoryConfiguration(context, Optional.empty(), EmfApi.URIConverter.PID, properties);
		return serviceHelper.createFactoryConfiguration(context, Optional.empty(), EmfApi.Resource_Factory_Registry.PID,
				properties);

	}

	// TODO: move to an Emf class?
	protected Configuration configureForResource(Map<String, Object> properties) throws Exception {
		configureForResourceSet(properties);
		return serviceHelper.createFactoryConfiguration(context, Optional.empty(), EmfApi.Resource.PID, properties);
	}

	// TODO: move to an Emf class?
	protected Configuration configureForResourceSet(Map<String, Object> properties) throws Exception {
		serviceHelper.createFactoryConfiguration(context, Optional.empty(), EmfApi.EPackage_Registry.PID, properties);
		return serviceHelper.createFactoryConfiguration(context, Optional.empty(), EmfApi.ResourceSet.PID, properties);
	}

	protected void configureForServerOrClient(Map<String, Object> properties) throws Exception {
		// Server & Client
		serviceHelper.createFactoryConfiguration(context, Optional.empty(), CdoApi.IDBAdapter.PID, properties);
		serviceHelper.createFactoryConfiguration(context, Optional.empty(), CdoApi.IDBConnectionProvider.PID,
				properties);
		serviceHelper.createFactoryConfiguration(context, Optional.empty(), CdoApi.IManagedContainer.PID, properties);
	}

	protected Configuration configureForServer(Map<String, Object> properties) throws Exception {
		configureForServerOrClient(properties);
		serviceHelper.createFactoryConfiguration(context, Optional.empty(), CdoApi.IAcceptor.PID, properties);
		return serviceHelper.createFactoryConfiguration(context, Optional.empty(), CdoApi.CdoServer.PID, properties);
	}

	protected Configuration configureForViewProvider(Map<String, Object> properties) throws Exception {
		configureForServer(properties);

		// Client
		serviceHelper.createFactoryConfiguration(context, Optional.empty(), CdoApi.IConnector.PID, properties);
		return serviceHelper.createFactoryConfiguration(context, Optional.empty(), CdoApi.CdoViewProvider.PID,
				properties);
	}

	protected void verify(CountDownLatch latch, Configuration configuration, String providerBundle, String providerType)
			throws Exception {
		assertTrue(latch.await(3000, TimeUnit.MILLISECONDS));
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

	protected void addLatchListener(CountDownLatch latch, String registeredTarget) {
		final CdoOsgiTestUtils.LatchServiceListener listener;
		listener = new CdoOsgiTestUtils.LatchServiceListener(latch, registeredTarget);
		context.addServiceListener(listener);
	}

	protected Map<String, Object> prep(TemporaryFolder tempFolder, String latchTo, CountDownLatch latch,
			String repoName, String description, String type, boolean includeClient, boolean ssl) throws Exception {
		// Assumes H2 is being used for DB testing
		File repoFile = tempFolder.newFile(repoName + CdoOsgiTestUtils.H2_SUFFIX);

		// Synchronize to when the CdoViewProvider is registered
		addLatchListener(latch, latchTo);

		// Prep
		final Map<String, Object> properties = new HashMap<>();
		testUtils.putTargets(properties, repoName);
		testUtils.putMinimalProperties(properties, repoName, description, type, testUtils.cleanH2FileUrl(repoFile));

		// An SSL container needs to be prepared minimally with the following
		if (includeClient && ssl) {
			final String password = "tooManySecrets";
			// And set CDO SSL properties (cleared in @After)
			System.setProperty(TRUST_PATH, createKeyStore(tempFolder, "truststore", password));
			System.setProperty(KEY_PATH, createKeyStore(tempFolder, "keystore", password));
			System.setProperty(CHECK_VALIDITY_CERTIFICATE, "false");
			System.setProperty(PASS_PHRASE, password);
		}
		return properties;
	}

}
