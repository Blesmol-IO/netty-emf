package io.blesmol.emf.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.impl.BinaryResourceImpl;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.osgi.service.log.LogService;

import io.blesmol.emf.api.EmfApi;

@SuppressWarnings({"deprecation"})
public class ProviderIntegrationTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	// TODO: break this out into different tests
	@Test
	public void shouldWork() throws Exception {

		final ResourceSetProvider rs = new ResourceSetProvider();
		final Map<String, Object> properties = new HashMap<>();

		// Setup URI converter
		final URIConverter uriConverter = new UriConverterProvider();
		final URI logicalUri = URI.createGenericURI("blesmol", "proxy", null);
		final URI tempUri = URI.createFileURI(folder.newFile().getAbsolutePath());
		uriConverter.getURIMap().put(logicalUri, tempUri);
		rs.setURIConverter(uriConverter);

		// Register a default binary resource impl
		final Resource.Factory resourceFactory = new Resource.Factory() {
			@Override
			public Resource createResource(URI uri) {
				return new BinaryResourceImpl(uri);
			}
		};

		final ResourceFactoryRegistryProvider resourceFactoryRegistry = new ResourceFactoryRegistryProvider();
		properties.put(EmfApi.CONTENT_TYPE, new String[] { Resource.Factory.Registry.DEFAULT_CONTENT_TYPE_IDENTIFIER });
		resourceFactoryRegistry.setFactory(resourceFactory, properties);
		resourceFactoryRegistry.setURIConverter(uriConverter);
		rs.setResourceFactoryRegistry(resourceFactoryRegistry);

		// Setup epackage registry provider with a dynamic package
		final EcoreFactory ecoreFactory = EcoreFactory.eINSTANCE;
		final EClass eClass = ecoreFactory.createEClass();
		eClass.setName("TestClass");

		final EPackage ePackage = ecoreFactory.createEPackage();
		ePackage.setName("TestPackage");
		ePackage.setNsPrefix("testPackage");
		ePackage.setNsURI("blesmol://test/");

		ePackage.getEClassifiers().add(eClass);
		final EPackageRegistryProvider ePackageRegistry = new EPackageRegistryProvider();
		properties.put(EmfApi.NS_URI, ePackage.getNsURI());

		ePackageRegistry.setEPackages(ePackage, properties);
		rs.setPackageRegistry(ePackageRegistry);

		// Create!
		final File file = new File(tempUri.toFileString());
		assertEquals(0L, file.length());

		ResourceProvider resourceProvider = new ResourceProvider();
		LogService mockService = mock(LogService.class);
		resourceProvider.logger = mockService;
		resourceProvider.resourceSet = rs;

		assertTrue(resourceProvider.delegate == null);
		resourceProvider.activate(new EmfApi.Resource() {

			@Override
			public Class<? extends Annotation> annotationType() {
				return EmfApi.Resource.class;
			}

			@Override
			public String emf_uri() {
				return logicalUri.toString();
			}

		});

		assertNotNull(resourceProvider.delegate);

		Resource resource = rs.getResource(logicalUri, false);
		assertEquals(resourceProvider.delegate, resource);

		//
		EObject eObject = ePackage.getEFactoryInstance().create(eClass);
		resource.getContents().add(eObject);
		resource.save(null);
		assertTrue(file.length() > 0);

		// Recreate & verify
		EObject other = resourceProvider.getContents().get(0);
		assertEquals(ePackage.getNsURI(), other.eClass().getEPackage().getNsURI());

	}

}
