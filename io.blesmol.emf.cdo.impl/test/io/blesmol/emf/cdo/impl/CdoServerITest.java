package io.blesmol.emf.cdo.impl;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.cdo.eresource.CDOResource;
import org.eclipse.emf.cdo.net4j.CDONet4jSession;
import org.eclipse.emf.cdo.net4j.CDONet4jSessionConfiguration;
import org.eclipse.emf.cdo.net4j.CDONet4jUtil;
import org.eclipse.emf.cdo.server.IRepository.Props;
import org.eclipse.emf.cdo.transaction.CDOTransaction;
import org.eclipse.net4j.connector.IConnector;
import org.eclipse.net4j.util.lifecycle.LifecycleUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import io.blesmol.emf.cdo.impl.CdoServerImpl;

public class CdoServerITest {

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	private CdoTestUtils cdoTestUtils = new CdoTestUtils();

	@Test
	public void shouldStartServer() throws Exception {

		// Setup server
		String repoName = getClass().getSimpleName();
		File tempFile = tempFolder.newFile(repoName);
		Map<String, String> repoProps = new HashMap<>();
		repoProps.put(Props.OVERRIDE_UUID, repoName);
		CdoServerImpl cdoServer = cdoTestUtils.server(tempFile, repoName, true, true, false, repoProps);

		// setup client
		IConnector connector = cdoTestUtils.getJvmConnector(cdoServer.container, repoName);
		CDONet4jSessionConfiguration configuration = CDONet4jUtil.createNet4jSessionConfiguration();
		configuration.setConnector(connector);
		configuration.setRepositoryName(repoName);
		CDONet4jSession session = configuration.openNet4jSession();
		CDOTransaction tx = session.openTransaction();
		CDOResource resource = tx.getOrCreateResource("/test");
		tx.commit();

		// Verify
		assertNotNull(resource);

		// Clean up
		LifecycleUtil.deactivate(session);
		cdoServer.deactivate();
	}

}
