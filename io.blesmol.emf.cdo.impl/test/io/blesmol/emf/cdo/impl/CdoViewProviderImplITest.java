package io.blesmol.emf.cdo.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.cdo.eresource.CDOResource;
import org.eclipse.emf.cdo.server.IRepository.Props;
import org.eclipse.emf.cdo.transaction.CDOTransaction;
import org.eclipse.emf.cdo.util.CDOURIData;
import org.eclipse.emf.cdo.util.InvalidURIException;
import org.eclipse.emf.cdo.view.CDOViewProvider;
import org.eclipse.emf.cdo.view.CDOViewProviderRegistry;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.net4j.util.container.IManagedContainer;
import org.eclipse.net4j.util.lifecycle.LifecycleUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import io.blesmol.emf.cdo.api.CdoApi;

public class CdoViewProviderImplITest {

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	private CdoTestUtils cdoTestUtils = new CdoTestUtils();

	@Test
	public void clientShouldConnectToServer() throws Exception {
		// Setup server
		String repoName = getClass().getSimpleName();
		File tempFile = tempFolder.newFile(repoName);
		Map<String, String> repoProps = new HashMap<>();
		repoProps.put(Props.OVERRIDE_UUID, repoName);
		CdoServerImpl cdoServer = cdoTestUtils.server(tempFile, repoName, true, true, false, repoProps);

		final String regex = "cdo:.*";
		final int priority = 500;

		// Setup view
		IManagedContainer clientContainer = cdoTestUtils.clientContainer();
		CdoViewProviderImpl viewProvider = new CdoViewProviderImpl();
		viewProvider.connector = cdoTestUtils.getJvmConnector(clientContainer, repoName);
		viewProvider.setRegex(regex);
		viewProvider.setPriority(priority);
		viewProvider.activate();
		URI uri = URI.createURI("cdo://notused:1234/" + repoName);
		ResourceSet rs = new ResourceSetImpl();
		CDOTransaction tx = (CDOTransaction) viewProvider.getView(uri, rs);
		CDOResource resource = tx.getOrCreateResource("/test");
		tx.commit();
		assertNotNull(resource);
		
		CDOViewProvider[] viewProviders = CDOViewProviderRegistry.INSTANCE.getViewProviders(uri);
		assertNotNull(viewProviders);
		assertTrue(viewProviders.length > 0);
		assertEquals(viewProvider, viewProviders[0]);

		// Clean up
		LifecycleUtil.deactivate(tx);
		LifecycleUtil.deactivate(viewProvider);
		cdoServer.deactivate();
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
		// cdo.net4j. ConnectorType :// [User [: Password] @] ConnectorSpecificAuthority / RepositoryName / ResourcePath 
		URI uri = URI.createURI(String.format("cdo.net4j.jvm://%s/%s/%s", expectedAcceptorName, expectedRepoName, expectedResourceName));
		CDOURIData uriData = new CDOURIData(uri);

		assertEquals(expectedRepoName, uriData.getRepositoryName());
		assertEquals(expectedResourceName, uriData.getResourcePath().toString());
		assertEquals(expectedAcceptorName, uriData.getAuthority());
		
		// No resource
		uri = URI.createURI(String.format("cdo.net4j.jvm://%s/%s/", expectedAcceptorName, expectedRepoName));
		uriData = new CDOURIData(uri);

		assertEquals(expectedRepoName, uriData.getRepositoryName());
		assertEquals("/", uriData.getResourcePath().toString());
		assertEquals(expectedAcceptorName, uriData.getAuthority());

	}

	@Test
	public void shouldSupportValidUriInRegex() {
		CdoViewProviderImpl viewProvider = new CdoViewProviderImpl();
		viewProvider.setRegex(CdoApi.CdoViewProvider.REGEX);
		viewProvider.setPriority(500);
		assertTrue(viewProvider.matchesRegex(URI.createURI("cdo://notused")));
		assertTrue(viewProvider.matchesRegex(URI.createURI("cdo.net4j.jvm://notused")));
		assertTrue(viewProvider.matchesRegex(URI.createURI("cdo.net4j.tcp://notused")));
		assertTrue(viewProvider.matchesRegex(URI.createURI("cdo.net4j.ssl://notused")));
		
	}
}
