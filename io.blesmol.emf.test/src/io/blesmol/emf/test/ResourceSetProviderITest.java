package io.blesmol.emf.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.impl.BinaryResourceImpl;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import io.blesmol.emf.api.EmfApi;
import io.blesmol.testutil.ServiceHelper;

@RunWith(MockitoJUnitRunner.class)
public class ResourceSetProviderITest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private final BundleContext context = FrameworkUtil.getBundle(ResourceSetProviderITest.class).getBundleContext();
	private ServiceHelper serviceHelper = new ServiceHelper();

	@After
	public void after() {
		serviceHelper.clear();
	}

	private Resource.Factory createAndRegisterResourceFactory() {
		// Register factory
		final Map<String, Object> properties = new HashMap<>();
		final Resource.Factory factory = new Resource.Factory() {
			@Override
			public Resource createResource(URI uri) {
				return new BinaryResourceImpl(uri);
			}
		};
		properties.put(EmfApi.CONTENT_TYPE, new String[] { Resource.Factory.Registry.DEFAULT_CONTENT_TYPE_IDENTIFIER });
		serviceHelper.registerService(context, Resource.Factory.class, factory, properties);
		return factory;
	}

	private static final String ECLASS_NAME = "TestClass";

	private EPackage createAndRegisterEPackage() {
		final Map<String, Object> properties = new HashMap<>();
		// Setup a dynamic EPackge
		final EcoreFactory ecoreFactory = EcoreFactory.eINSTANCE;
		final EClass eClass = ecoreFactory.createEClass();
		eClass.setName(ECLASS_NAME);

		final EPackage ePackage = ecoreFactory.createEPackage();
		ePackage.setName("TestPackage");
		ePackage.setNsPrefix("testPackage");
		final String expectedUri = "blesmol://test/";
		ePackage.setNsURI(expectedUri);
		ePackage.getEClassifiers().add(eClass);
		properties.put(EmfApi.NS_URI, ePackage.getNsURI());

		// Register EPackage as a service
		serviceHelper.registerService(context, EPackage.class, ePackage, properties);
		return ePackage;
	}

	@Test
	public void shouldRegisterFactory() throws Exception {
		serviceHelper.createFactoryConfiguration(context, Optional.empty(), EmfApi.URIConverter.PID,
				Collections.emptyMap());
		serviceHelper.createFactoryConfiguration(context, Optional.empty(), EmfApi.Resource_Factory_Registry.PID,
				Collections.emptyMap());
		final Resource.Factory expectedFactory = createAndRegisterResourceFactory();

		final Resource.Factory.Registry rfRegistry = serviceHelper.getService(context, Resource.Factory.Registry.class,
				Optional.empty(), 250);
		assertNotNull(rfRegistry);
		final Resource.Factory actualFactory = rfRegistry.getFactory(URI.createGenericURI("test", "opaque", null),
				"foo/bar");
		assertEquals(expectedFactory, actualFactory);
	}

	@Test
	public void shouldRegisterEPackage() throws Exception {
		serviceHelper.createFactoryConfiguration(context, Optional.empty(), EmfApi.EPackage_Registry.PID,
				Collections.emptyMap());
		final EPackage expectedPackage = createAndRegisterEPackage();
		EPackage.Registry ePackageRegistry = serviceHelper.getService(context, EPackage.Registry.class,
				Optional.empty(), 250);
		assertNotNull(ePackageRegistry);
		EPackage actualPackage = ePackageRegistry.getEPackage(expectedPackage.getNsURI());
		assertEquals(expectedPackage.getName(), actualPackage.getName());
	}

	@Test
	public void shouldGetUriConverter() throws Exception {
		serviceHelper.createFactoryConfiguration(context, Optional.empty(), EmfApi.URIConverter.PID,
				Collections.emptyMap());
		URIConverter uriConverter = serviceHelper.getService(context, URIConverter.class, Optional.empty(), 250);
		assertNotNull(uriConverter);
	}

	@Test
	public void shouldSaveBinaryResource() throws Exception {
		serviceHelper.createFactoryConfiguration(context, Optional.empty(), EmfApi.URIConverter.PID,
				Collections.emptyMap());
		serviceHelper.createFactoryConfiguration(context, Optional.empty(), EmfApi.Resource_Factory_Registry.PID,
				Collections.emptyMap());
		serviceHelper.createFactoryConfiguration(context, Optional.empty(), EmfApi.EPackage_Registry.PID,
				Collections.emptyMap());
		serviceHelper.createFactoryConfiguration(context, Optional.empty(), EmfApi.ResourceSet.PID,
				Collections.emptyMap());

		// Prep
		final EPackage ePackage = createAndRegisterEPackage();
		final EClass eClass = (EClass) ePackage.getEClassifier(ECLASS_NAME);
		final EObject eObject = ePackage.getEFactoryInstance().create(eClass);

		final URI logicalUri = URI.createGenericURI("blesmol", "proxy", null);
		final URI tempUri = URI.createFileURI(folder.newFile().getAbsolutePath());
		final URIConverter uriConverter = serviceHelper.getService(context, URIConverter.class, Optional.empty(), 100);
		uriConverter.getURIMap().put(logicalUri, tempUri);

		createAndRegisterResourceFactory();

		final ResourceSet rs = serviceHelper.getService(context, ResourceSet.class, Optional.empty(), 100);

		// Verify
		assertNotNull(rs);
		Resource resource = rs.createResource(logicalUri);
		resource.getContents().add(eObject);
		resource.save(null);

		// Recreate & verify
		rs.getResources().clear();
		resource = rs.getResource(logicalUri, true);
		EObject other = resource.getContents().get(0);
		assertEquals(ePackage.getNsURI(), other.eClass().getEPackage().getNsURI());

	}

}