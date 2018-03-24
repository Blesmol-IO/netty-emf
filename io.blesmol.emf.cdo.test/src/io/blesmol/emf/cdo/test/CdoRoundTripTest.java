package io.blesmol.emf.cdo.test;

import static org.junit.Assert.assertNotNull;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.Resource.Factory;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.osgi.framework.Constants;
import org.osgi.service.cm.Configuration;

public class CdoRoundTripTest extends AbstractTest {

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@After
	public void after() {
		super.after();
	}

	@Test
	public void shouldSaveEmfObjectToCdoResourceViaJvmServer() throws Exception {

		final String repoName = "jvmRoundtrip";
		final String description = getClass().getName();
		final String type = "jvm";
		final String resourceUri = "cdo://jvmserver/jvmRoundtrip";

		// Prep & configure
		final CountDownLatch viewProviderLatch = new CountDownLatch(1);
		final CountDownLatch resourceFactoryLatch = new CountDownLatch(1);
		final Map<String, Object> properties = prep(tempFolder, VIEW_PROVIDER_TYPE, viewProviderLatch, repoName,
				description, type, true, false);
		addLatchListener(resourceFactoryLatch, RESOURCE_FACTORY_TYPE);
		testUtils.putEmfProperties(properties, resourceUri);
		final Configuration viewProvider = configureForViewProvider(properties);

		// Latch to view provider before continuing
		verify(viewProviderLatch, viewProvider, CDO_PROVIDER_BUNDLE, VIEW_PROVIDER_TYPE);

		final Configuration resourceFactoryConfig = configureForResourceFactory(properties);
		// Latch to resource factory before continuing
		verify(resourceFactoryLatch, resourceFactoryConfig, EMF_PROVIDER_BUNDLE, RESOURCE_FACTORY_TYPE);

		final Configuration resourceConfig = configureForResource(properties);

		// Get a resource set
		String resourceFilter = String.format("(%s=%s)", Constants.SERVICE_PID, resourceConfig.getPid());
		Resource resource = serviceHelper.getService(context, Resource.class, Optional.of(resourceFilter), 250);
		assertNotNull(resource);

	}

}
