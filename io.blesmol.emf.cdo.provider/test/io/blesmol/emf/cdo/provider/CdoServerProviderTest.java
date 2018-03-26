package io.blesmol.emf.cdo.provider;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.cdo.server.IRepository.Props;
import org.eclipse.net4j.util.container.IManagedContainer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import io.blesmol.emf.cdo.api.CdoApi;

public class CdoServerProviderTest {

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	ProviderCdoTestUtils cdoTestUtils = new ProviderCdoTestUtils();

	@Test
	public void shouldCreateServerProvider() throws Exception {

		// Prep
		String repoName = getClass().getSimpleName();
		CdoApi.CdoServer serverConfig = mock(CdoApi.CdoServer.class);
		when(serverConfig.blesmol_cdoserver_reponame()).thenReturn(repoName);
		Map<String, Object> props = new HashMap<>();
		props.put(Props.OVERRIDE_UUID, "");
		final IManagedContainer container = cdoTestUtils.container("jvm");

		// Verify
		CdoServerProvider serverProvider = cdoTestUtils.server(serverConfig, cdoTestUtils.serverContainer(container),
				cdoTestUtils.repoFile(tempFolder, repoName), repoName, true, true, false, props);

		// Clean up
		serverProvider.deactivate();
	}

}
