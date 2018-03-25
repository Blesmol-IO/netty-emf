package io.blesmol.emf.cdo.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
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
import org.eclipse.net4j.util.lifecycle.LifecycleUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import io.blesmol.emf.cdo.api.CdoApi;
import io.blesmol.emf.test.util.EmfTestUtils;

public class CdoViewProviderImplITest {

	private EmfTestUtils emfTestUtils = new EmfTestUtils();
	private ImplCdoTestUtils cdoTestUtils = new ImplCdoTestUtils();

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Test
	public void shouldSaveGetEObjectTransparentlyViaViewProvider() throws Exception {
		// Setup server
		String repoName = getClass().getSimpleName();
		File tempFile = tempFolder.newFile(repoName);
		Map<String, String> repoProps = new HashMap<>();
		repoProps.put(Props.OVERRIDE_UUID, repoName);
		final CdoServerImpl cdoServer = cdoTestUtils.server(tempFile, repoName, true, true, false, repoProps);

		// Setup view
		final CdoViewProviderImpl viewProvider = new CdoViewProviderImpl();
		viewProvider.container = cdoTestUtils.jvmClientContainer();
		viewProvider.setRegex(CdoApi.CdoViewProvider.REGEX);
		viewProvider.setPriority(1000);
		viewProvider.activate();
		final String resourceName = "/test";
		URI uri = URI.createURI(
				String.format("cdo.net4j.jvm://%s/%s/%s?transactional=true", repoName, repoName, resourceName));
		ResourceSet rs = cdoTestUtils.createAndPrepResourceSet(ImplCdoTestUtils.SCHEMA_JVM);
		Resource resource = rs.createResource(uri);
		assertNotNull(resource);

		final EObject expectedEObject = emfTestUtils.eObject("TestObject", "testAttribute",
				EcorePackage.Literals.ESTRING, "TestPackage", "testPackage", "test://someTest/package");
		resource.getContents().add(expectedEObject);
		resource.save(null);

		rs = null;
		resource = null;
		rs = cdoTestUtils.createAndPrepResourceSet(ImplCdoTestUtils.SCHEMA_JVM);
		resource = rs.getResource(uri, true);
		Object object = resource.getContents().get(0);
		assertTrue(object instanceof EObject);
		emfTestUtils.assertEObjects(expectedEObject, (EObject) object);

		// Clean up
		// LifecycleUtil.deactivate(tx);
		LifecycleUtil.deactivate(viewProvider);
		cdoServer.deactivate();
	}

	@Test
	public void shouldContainViewProvider() throws Exception {

		CdoViewProviderImpl viewProvider = new CdoViewProviderImpl();
		viewProvider.container = Mockito.mock(IManagedContainer.class);
		viewProvider.setRegex(CdoApi.CdoViewProvider.REGEX);
		viewProvider.setPriority(1000);
		viewProvider.activate();
		CDOViewProvider[] viewProviders = CDOViewProviderRegistry.INSTANCE
				.getViewProviders(URI.createURI("cdo.net4j.jvm://notused/notused/notused/"));
		assertNotNull(viewProviders);
		assertTrue(viewProviders.length > 0);

		// Throws exception if not found
		Arrays.stream(viewProviders).filter(vp -> vp.toString().equals(viewProvider.toString())).findFirst().get();

		LifecycleUtil.deactivate(viewProvider);

	}

	@Test
	public void shouldMatchRegex() {
		CdoViewProviderImpl impl = new CdoViewProviderImpl();
		impl.setRegex(CdoApi.CdoViewProvider.REGEX);
		assertEquals("jvm", impl.getTransport("cdo.net4j.jvm"));
	}

	@Test
	public void shouldHaveInvalidUri() {
		CdoViewProviderImpl viewProvider = new CdoViewProviderImpl();

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
		// cdo.net4j. ConnectorType :// [User [: Password] @] ConnectorSpecificAuthority
		// / RepositoryName / ResourcePath
		URI uri = URI.createURI(String.format("cdo.net4j.jvm://%s/%s/%s", expectedAcceptorName, expectedRepoName,
				expectedResourceName));
		CDOURIData uriData = new CDOURIData(uri);

		assertEquals(expectedRepoName, uriData.getRepositoryName());
		assertEquals(expectedResourceName, uriData.getResourcePath().toString());
		assertEquals(expectedAcceptorName, uriData.getAuthority());

	}

	@Test
	public void shouldSupportValidUriInRegex() {
		CdoViewProviderImpl viewProvider = new CdoViewProviderImpl();
		viewProvider.setRegex(CdoApi.CdoViewProvider.REGEX);
		viewProvider.setPriority(500);
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

		CdoViewProviderImpl viewProvider = new CdoViewProviderImpl();
		viewProvider.setRegex(CdoApi.CdoViewProvider.REGEX);
		viewProvider.setPriority(1000);

		assertEquals(String.format("%s", expectedResourceName), viewProvider.getPath(uri));

	}
}
