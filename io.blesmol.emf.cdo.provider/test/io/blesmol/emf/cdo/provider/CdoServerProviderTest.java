package io.blesmol.emf.cdo.provider;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.eclipse.emf.cdo.server.IRepository.Props;
import org.eclipse.net4j.db.IDBAdapter;
import org.eclipse.net4j.util.container.IManagedContainer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import io.blesmol.emf.cdo.api.CdoApi;

public class CdoServerProviderTest {

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	CdoTestUtils cdoTestUtils = new CdoTestUtils();

	@Test
	public void shouldCreateServerProvider() throws Exception {
		
		String repoName = getClass().getSimpleName();
		CdoApi.CdoServer config = mock(CdoApi.CdoServer.class);
		when(config.blesmol_cdoserver_reponame()).thenReturn(repoName);

		File tempFile = tempFolder.newFile(repoName);
		Map<String, Object> props = new HashMap<>();
		props.put(Props.OVERRIDE_UUID, repoName);

		DataSource dataSource = cdoTestUtils.dataSource(tempFile, repoName);
		IDBAdapter dbAdapter = cdoTestUtils.h2Adapter();
		CdoServerProvider serverProvider = new CdoServerProvider();
		final IManagedContainer container = cdoTestUtils.serverContainer(true);		
		serverProvider.setDbConnectionProvider(dbAdapter.createConnectionProvider(dataSource));
		serverProvider.setContainer(container);
		serverProvider.setAcceptor(cdoTestUtils.getJvmAcceptor(container, repoName));
		serverProvider.setDbAdapter(dbAdapter);
		
		// Verify
		serverProvider.activate(config, props);
		serverProvider.deactivate();
	}

}
