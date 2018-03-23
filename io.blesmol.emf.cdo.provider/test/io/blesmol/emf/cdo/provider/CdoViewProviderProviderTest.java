package io.blesmol.emf.cdo.provider;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.eclipse.emf.cdo.eresource.CDOResource;
import org.eclipse.emf.cdo.server.IRepository.Props;
import org.eclipse.emf.cdo.transaction.CDOTransaction;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.net4j.db.IDBAdapter;
import org.eclipse.net4j.util.container.IManagedContainer;
import org.eclipse.net4j.util.lifecycle.LifecycleUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import io.blesmol.emf.cdo.api.CdoApi;

public class CdoViewProviderProviderTest {

	CdoTestUtils cdoTestUtils = new CdoTestUtils();

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Test
	public void shouldGetResource() throws Exception {
		String repoName = getClass().getSimpleName();
		File tempFile = tempFolder.newFile(repoName);

		// Mock server configs
		CdoApi.CdoServer config = mock(CdoApi.CdoServer.class);
		when(config.blesmol_cdoserver_reponame()).thenReturn(repoName);
		Map<String, Object> props = new HashMap<>();
		props.put(Props.OVERRIDE_UUID, repoName);

		// Create server
		CdoServerProvider serverProvider = new CdoServerProvider();

		IDBAdapter dbAdapter = cdoTestUtils.h2Adapter();
		DataSource dataSource = cdoTestUtils.dataSource(tempFile, repoName);

		final IManagedContainer container = cdoTestUtils.serverContainer(true);
		serverProvider.setDbConnectionProvider(dbAdapter.createConnectionProvider(dataSource));
		serverProvider.setContainer(container);
		serverProvider.setAcceptor(cdoTestUtils.getJvmAcceptor(container, repoName));
		serverProvider.setDbAdapter(dbAdapter);
		serverProvider.activate(config, props);
		
		// Mock view config
		CdoApi.CdoViewProvider viewConfig = mock(CdoApi.CdoViewProvider.class);
		when(viewConfig.blesmol_cdoviewprovider_regex()).thenReturn("cdo:.*");
		when(viewConfig.blesmol_cdoviewprovider_priority()).thenReturn(500);
		
		CdoViewProviderProvider viewProvider = new CdoViewProviderProvider();
		viewProvider.setConnector(cdoTestUtils.getJvmConnector(container, repoName));
		viewProvider.activate(viewConfig, Collections.emptyMap());

		final URI uri = URI.createURI("cdo://notused:1234/" + repoName);
		final ResourceSet rs = new ResourceSetImpl();
		final CDOTransaction tx = (CDOTransaction)viewProvider.getView(uri, rs);
		final CDOResource resource = tx.getOrCreateResource("/test");
		tx.commit();
		assertNotNull(resource);

		// Clean up
		LifecycleUtil.deactivate(tx);
		LifecycleUtil.deactivate(viewProvider);
		serverProvider.deactivate();
		


	}

}
