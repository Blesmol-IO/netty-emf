package io.blesmol.emf.cdo.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.cdo.server.IRepository.Props;
import org.eclipse.emf.cdo.util.CDOURIData;
import org.eclipse.emf.cdo.util.InvalidURIException;
import org.eclipse.emf.cdo.view.CDOViewProvider;
import org.eclipse.emf.cdo.view.CDOViewProviderRegistry;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.net4j.util.container.IManagedContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import io.blesmol.emf.cdo.api.CdoApi;
import io.blesmol.emf.test.util.EmfTestUtils;

public class CdoViewProviderImplITest {

	public static final String H2_SUFFIX = ".mv.db";
	private EmfTestUtils emfTestUtils = new EmfTestUtils();
	private ImplCdoTestUtils cdoTestUtils = new ImplCdoTestUtils();
	private static final String REPO_NAME = CdoViewProviderImplITest.class.getSimpleName();
	private static final Map<String, String> REPO_PROPS = new HashMap<>();

	private CdoViewProviderImpl viewProvider;
	private CdoServerImpl cdoServer;

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@BeforeClass
	public static void beforeClass() {
		REPO_PROPS.put(Props.OVERRIDE_UUID, "");
	}

	@Before
	public void before() throws Exception {
		IManagedContainer container = cdoTestUtils.container("jvm");
		viewProvider = new CdoViewProviderImpl();
		viewProvider.container = cdoTestUtils.clientContainer(container);
		viewProvider.setRegex(CdoApi.CdoViewProvider.REGEX);
		viewProvider.setPriority(1000);
		viewProvider.activate();

		cdoServer = cdoTestUtils.server(cdoTestUtils.serverContainer(container),
				cdoTestUtils.repoFile(tempFolder, REPO_NAME), REPO_NAME, true, true, false, REPO_PROPS);
	}

	@After
	public void after() {
		viewProvider.container.deactivate();
		viewProvider.deactivate();
		cdoServer.dbAdapter = null;
		cdoServer.connectionProvider = null;
		cdoServer.acceptor = null;
		// cdoServer.container.deactivate();
		cdoServer.deactivate();
		viewProvider = null;
		cdoServer = null;
	}

	@Test
	public void shouldSaveGetEObjectTransparentlyViaViewProvider() throws Exception {
		final String resourceName = "/test";
		URI uri = URI.createURI(
				String.format("cdo.net4j.jvm://%s/%s/%s?transactional=true", REPO_NAME, REPO_NAME, resourceName));
		ResourceSet rs = cdoTestUtils.createAndPrepResourceSet(ImplCdoTestUtils.SCHEMA_JVM);

		Resource resource = rs.createResource(uri);
		assertNotNull(resource);

		final EObject expectedEObject = emfTestUtils.eObject("TestObject", "testAttribute",
				EcorePackage.Literals.ESTRING, "TestPackage", "testPackage", "test://someTest/package");
		resource.getContents().add(expectedEObject);
		resource.save(null);

		after();
		rs = null;
		resource = null;
		before();

		rs = cdoTestUtils.createAndPrepResourceSet(ImplCdoTestUtils.SCHEMA_JVM);
		resource = rs.getResource(uri, true);
		Object object = resource.getContents().get(0);
		assertTrue(object instanceof EObject);
		emfTestUtils.assertEObjects(expectedEObject, (EObject) object);
		tempFolder.delete();
	}

	@Test
	public void shouldContainViewProvider() throws Exception {

		CDOViewProvider[] viewProviders = CDOViewProviderRegistry.INSTANCE
				.getViewProviders(URI.createURI("cdo.net4j.jvm://notused/notused/notused/"));
		assertNotNull(viewProviders);
		assertTrue(viewProviders.length > 0);

		// Throws exception if not found
		Arrays.stream(viewProviders).filter(vp -> vp.toString().equals(viewProvider.toString())).findFirst().get();
	}

	@Test
	public void shouldMatchRegex() {
		assertEquals("jvm", viewProvider.getTransport("cdo.net4j.jvm"));
	}

	@Test
	public void shouldHaveInvalidUri() {
		// No resource
		URI uri = URI.createURI("cdo://bob");
		try {
			viewProvider.getView(uri, null);
			fail();
		} catch (InvalidURIException e) {
		}
		// Other tests?

	}

	@Test
	public void shouldHaveValidUri() {
		final String expectedRepoName = "repository";
		final String expectedResourceName = "resource";
		final String expectedAcceptorName = expectedRepoName; // "acceptor";

		URI uri = URI.createURI(String.format("cdo.net4j.jvm://%s/%s/%s", expectedAcceptorName, expectedRepoName,
				expectedResourceName));
		CDOURIData uriData = new CDOURIData(uri);

		assertEquals(expectedRepoName, uriData.getRepositoryName());
		assertEquals(expectedResourceName, uriData.getResourcePath().toString());
		assertEquals(expectedAcceptorName, uriData.getAuthority());
	}

	@Test
	public void shouldSupportValidUriInRegex() {
		assertTrue(viewProvider.matchesRegex(URI.createURI("cdo.net4j.jvm://notused")));
		assertTrue(viewProvider.matchesRegex(URI.createURI("cdo.net4j.tcp://notused")));
		assertTrue(viewProvider.matchesRegex(URI.createURI("cdo.net4j.ssl://notused")));
	}

	@Test
	public void shouldParseConnectionAwareUriCorrectly() {
		final String expectedRepoName = "repository";
		final String expectedResourceName = "resource";
		final String expectedAcceptorName = expectedRepoName; // "acceptor";

		// No resource
		URI uri = URI.createURI(String.format("cdo.net4j.jvm://%s/%s/%s", expectedAcceptorName, expectedRepoName,
				expectedResourceName));

		assertEquals(String.format("%s", expectedResourceName), viewProvider.getPath(uri));
	}
}
