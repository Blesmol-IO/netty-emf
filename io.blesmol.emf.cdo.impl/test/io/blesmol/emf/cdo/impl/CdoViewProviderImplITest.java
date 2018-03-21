package io.blesmol.emf.cdo.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.cdo.eresource.CDOResource;
import org.eclipse.emf.cdo.server.IRepository.Props;
import org.eclipse.emf.cdo.transaction.CDOTransaction;
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

import io.blesmol.emf.cdo.impl.CdoServerImpl;
import io.blesmol.emf.cdo.impl.CdoViewProviderImpl;

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

}
