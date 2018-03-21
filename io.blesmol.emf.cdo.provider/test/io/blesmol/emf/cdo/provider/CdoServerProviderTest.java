package io.blesmol.emf.cdo.provider;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.cdo.server.IRepository.Props;
import org.eclipse.net4j.util.container.IManagedContainer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import io.blesmol.emf.cdo.provider.CdoTestUtils;
import io.blesmol.emf.cdo.api.CdoApi;

public class CdoServerProviderTest {

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	CdoTestUtils cdoTestUtils = new CdoTestUtils();

	@Test
	public void shouldCreateServerProvider() throws Exception {
		
		String repoName = getClass().getSimpleName();
		CdoApi.CdoServer config = mock(CdoApi.CdoServer.class);
		when(config.repoName()).thenReturn(repoName);

		File tempFile = tempFolder.newFile(repoName);
		Map<String, Object> props = new HashMap<>();
		props.put(Props.OVERRIDE_UUID, repoName);

		CdoServerProvider serverProvider = new CdoServerProvider();
		serverProvider.setDataSource(cdoTestUtils.dataSource(tempFile, repoName));
		final IManagedContainer container = cdoTestUtils.serverContainer(true);
		serverProvider.setContainer(container);
		serverProvider.setAcceptor(cdoTestUtils.getJvmAcceptor(container, repoName));
		serverProvider.setDbAdapter(cdoTestUtils.h2Adapter());
		serverProvider.activate(config, props);
		serverProvider.deactivate();
	}

}
