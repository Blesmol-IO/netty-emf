package io.blesmol.emf.cdo.impl;

import java.io.File;
import java.util.Map;

import org.eclipse.net4j.util.container.IManagedContainer;

import io.blesmol.emf.cdo.test.util.CdoTestUtils;

public class ImplCdoTestUtils extends CdoTestUtils {

	public CdoServerImpl server(IManagedContainer container, File tempFile, String repoName, boolean auditing, boolean branching, boolean withRanges,
			Map<String, String> repoProps) throws Exception {
		CdoServerImpl cdoServer = new CdoServerImpl();
		cdoServer.dbAdapter = h2Adapter();
		cdoServer.connectionProvider = cdoServer.dbAdapter.createConnectionProvider(dataSource(tempFile, repoName));
		cdoServer.container = serverContainer(container);
		cdoServer.acceptor = getJvmAcceptor(cdoServer.container, repoName);

		// XMI bundle needs to be on run path for internal CDO classes to work
		// Adding a '*' to the global resource factory registry doesn't seem to work
		cdoServer.activate(repoName, auditing, branching, withRanges, repoProps);
		return cdoServer;
	}

	@Override
	public IManagedContainer container(String type) {
		DelegatedContainer container = new DelegatedContainer();
		container.activate(type);
		return container;
	}
}
