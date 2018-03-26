package io.blesmol.emf.cdo.provider;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.cdo.server.IRepository.Props;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.osgi.service.log.LogService;

import io.blesmol.emf.cdo.api.CdoApi;
import io.blesmol.emf.cdo.test.util.CdoTestUtils;
import io.blesmol.emf.test.util.EmfTestUtils;

public class CdoViewProviderProviderTest {

	private EmfTestUtils emfTestUtils = new EmfTestUtils();
	private ProviderCdoTestUtils cdoTestUtils = new ProviderCdoTestUtils();

	private CdoViewProviderProvider viewProvider;
	private CdoServerProvider cdoServer;
	private DelegatedContainerProvider container;

	private final String repoName = getClass().getSimpleName();
	private static final Map<String, Object> REPO_PROPS = new HashMap<>();
	private static final CdoApi.CdoServer serverConfig = mock(CdoApi.CdoServer.class);
	private static final CdoApi.CdoViewProvider viewConfig = mock(CdoApi.CdoViewProvider.class);

	@BeforeClass
	public static void beforeClass() {
		REPO_PROPS.put(Props.OVERRIDE_UUID, "");
	}

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Before
	public void before() throws Exception {
		container = (DelegatedContainerProvider) cdoTestUtils.container("jvm");

		when(viewConfig.blesmol_cdoviewprovider_regex()).thenReturn(CdoApi.CdoViewProvider.REGEX);
		when(viewConfig.blesmol_cdoviewprovider_priority()).thenReturn(1000);
		when(serverConfig.blesmol_cdoserver_reponame()).thenReturn(repoName);
		viewProvider = new CdoViewProviderProvider();
		viewProvider.setContainer(cdoTestUtils.clientContainer(container));
		viewProvider.setRegex(CdoApi.CdoViewProvider.REGEX);
		viewProvider.setPriority(1000);
		viewProvider.logger = Mockito.mock(LogService.class);
		viewProvider.activate(viewConfig, Collections.emptyMap());

		cdoServer = cdoTestUtils.server(serverConfig, cdoTestUtils.serverContainer(container),
				cdoTestUtils.repoFile(tempFolder, repoName), repoName, true, true, false, REPO_PROPS);
	}

	@After
	public void after() {
		container.deactivate(null);
		viewProvider.deactivate();
		cdoServer.unsetDbAdapter(null);
		cdoServer.unsetDbConnectionProvider(null);
		cdoServer.unsetAcceptor(null);
		cdoServer.deactivate();
		viewProvider = null;
		cdoServer = null;
	}

	/**
	 * FIXME: most of this is copy pasta
	 * 
	 * @see io.blesmol.emf.cdo.impl.CdoViewProviderImplITest#shouldSaveGetEObjectTransparentlyViaViewProvider()
	 */
	@Test
	public void shouldSaveGetEObjectTransparentlyViaViewProvider() throws Exception {

		final String resourceName = "/test";
		URI uri = URI.createURI(
				String.format("cdo.net4j.jvm://%s/%s/%s?transactional=true", repoName, repoName, resourceName));
		ResourceSet rs = cdoTestUtils.createAndPrepResourceSet(CdoTestUtils.SCHEMA_JVM);
		Resource resource = rs.createResource(uri);
		assertNotNull(resource);

		final EObject expectedEObject = emfTestUtils.eObject("TestObject", "testAttribute",
				EcorePackage.Literals.ESTRING, "TestPackage", "testPackage", "test://someTest/package");
		resource.getContents().add(expectedEObject);
		resource.save(null);

		rs = null;
		resource = null;
		after();
		before();

		rs = cdoTestUtils.createAndPrepResourceSet(CdoTestUtils.SCHEMA_JVM);
		resource = rs.getResource(uri, true);
		Object object = resource.getContents().get(0);
		assertTrue(object instanceof EObject);
		emfTestUtils.assertEObjects(expectedEObject, (EObject) object);

	}

}
