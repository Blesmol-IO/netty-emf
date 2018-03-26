package io.blesmol.emf.cdo.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.Constants;
import org.osgi.service.cm.Configuration;

import io.blesmol.emf.test.util.EmfTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class CdoRoundTripTest extends AbstractTest {

	EmfTestUtils emfTestUtils = new EmfTestUtils();

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@After
	public void after() {
		super.after();
	}

	private ResourceSet initAndCreateResourceSet(String repoName, String description, String type, String resourceUri)
			throws Exception {
		final CountDownLatch viewProviderLatch = new CountDownLatch(1);
		final CountDownLatch resourceFactoryLatch = new CountDownLatch(1);
		final Map<String, Object> properties = prep(tempFolder, VIEW_PROVIDER_TYPE, viewProviderLatch, repoName,
				description, type, /* includeClient */ true, false);
		addLatchListener(resourceFactoryLatch, RESOURCE_FACTORY_TYPE);
		testUtils.putEmfProperties(properties, resourceUri);
		final Configuration viewProvider = configureForViewProvider(properties);

		// Latch to view provider before continuing
		verify(viewProviderLatch, viewProvider, CDO_PROVIDER_BUNDLE, VIEW_PROVIDER_TYPE);

		final Configuration resourceFactoryConfig = configureForResourceFactory(properties);
		// Latch to resource factory before continuing
		verify(resourceFactoryLatch, resourceFactoryConfig, EMF_PROVIDER_BUNDLE, RESOURCE_FACTORY_TYPE);

		// final Configuration resourceConfig = configureForResource(properties);
		final Configuration resourceSetConfig = configureForResourceSet(properties);

		// Get a resource
		String rsFilter = String.format("(%s=%s)", Constants.SERVICE_PID, resourceSetConfig.getPid());
		return serviceHelper.getService(context, ResourceSet.class, Optional.of(rsFilter), 2000);
	}

	@Test
	public void shouldSaveEmfObjectToCdoResourceViaJvmServer() throws Exception {

		final String repoName = "tcpRepo";
		final String description = "127.0.0.1:2036";
		final String authority = description;
		final String type = "tcp";
		final String resourceName = "resourceName";
		final String resourceUri = String.format("cdo.net4j.%s://%s/%s/%s?transactional=true", type, authority, repoName,
				resourceName);

		// Obtain resourceset
		ResourceSet rs = initAndCreateResourceSet(repoName, description, type, resourceUri);
		assertNotNull(rs);

		final EObject expectedEObject = emfTestUtils.eObject("TestObject", "testAttribute",
				EcorePackage.Literals.ESTRING, "TestPackage", "testPackage", "test://someTest/package");
		final URI uri = URI.createURI(resourceUri);
		Resource resource = rs.createResource(uri);
		resource.getContents().add(expectedEObject);
		resource.save(null);

		// Clear configs
		rs = null;
		resource = null;
		this.after();

		// Recreate resource
		rs = initAndCreateResourceSet(repoName, description, type, resourceUri);
		assertNotNull(rs);

		// Read the object
		resource = rs.getResource(uri, true);
		Object object = resource.getContents().get(0);
		assertTrue(object instanceof EObject);
		emfTestUtils.assertEObjects(expectedEObject, (EObject) object);

	}

}
