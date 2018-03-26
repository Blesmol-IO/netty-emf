package io.blesmol.emf.cdo.provider;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import org.eclipse.net4j.db.IDBAdapter;
import org.eclipse.net4j.util.container.IManagedContainer;
import org.mockito.Mockito;
import org.osgi.service.log.LogService;

import io.blesmol.emf.cdo.api.CdoApi;
import io.blesmol.emf.cdo.test.util.CdoTestUtils;

public class ProviderCdoTestUtils extends CdoTestUtils {

	public DelegatedContainerProvider container(String type) {
		DelegatedContainerProvider container = new DelegatedContainerProvider();
		container.logger = Mockito.mock(LogService.class);
		CdoApi.IManagedContainer config = mock(CdoApi.IManagedContainer.class);
		when(config.emf_cdo_managedcontainer_type()).thenReturn(type);
		container.activate(config, Collections.emptyMap());
		return container;
	}
	
	public CdoServerProvider server(CdoApi.CdoServer config, IManagedContainer container, File tempFile, String repoName, boolean auditing, boolean branching, boolean withRanges,
			Map<String, Object> repoProps) throws Exception {
		CdoServerProvider cdoServer = new CdoServerProvider();
		IDBAdapter dbAdapter = h2Adapter();
		IManagedContainer serverContainer = serverContainer(container);
		cdoServer.setDbAdapter(dbAdapter);
		cdoServer.setDbConnectionProvider(dbAdapter.createConnectionProvider(dataSource(tempFile, repoName)));
		cdoServer.setContainer(serverContainer);
		cdoServer.setAcceptor(getJvmAcceptor(serverContainer, repoName));

		// XMI bundle needs to be on run path for internal CDO classes to work
		// Adding a '*' to the global resource factory registry doesn't seem to work
		cdoServer.activate(config, repoProps);
		return cdoServer;
	}
}
