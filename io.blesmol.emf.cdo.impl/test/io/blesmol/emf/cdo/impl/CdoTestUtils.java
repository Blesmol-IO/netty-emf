package io.blesmol.emf.cdo.impl;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Map;

import javax.sql.DataSource;

import org.eclipse.emf.cdo.net4j.CDONet4jUtil;
import org.eclipse.emf.cdo.server.net4j.CDONet4jServerUtil;
import org.eclipse.net4j.Net4jUtil;
import org.eclipse.net4j.acceptor.IAcceptor;
import org.eclipse.net4j.connector.IConnector;
import org.eclipse.net4j.db.IDBAdapter;
import org.eclipse.net4j.db.h2.H2Adapter;
import org.eclipse.net4j.jvm.JVMUtil;
import org.eclipse.net4j.util.container.ContainerUtil;
import org.eclipse.net4j.util.container.IManagedContainer;
import org.h2.jdbcx.JdbcDataSource;

import io.blesmol.emf.cdo.impl.CdoServerImpl;

public class CdoTestUtils {

	public IManagedContainer serverContainer(boolean useJvm) {
		IManagedContainer container = ContainerUtil.createContainer();

		Net4jUtil.prepareContainer(container); // Register Net4j factories

		JVMUtil.prepareContainer(container); // Register JVM factories

		CDONet4jServerUtil.prepareContainer(container); // Register CDO server factories
		container.activate(); // standalone
		return container;
	}

	public IManagedContainer clientContainer() {
		IManagedContainer container = ContainerUtil.createContainer();

		Net4jUtil.prepareContainer(container); // Register Net4j factories

		JVMUtil.prepareContainer(container); // Register JVM factories
		// TCPUtil.prepareContainer(container);

		CDONet4jUtil.prepareContainer(container); // Register CDO client factories
		container.activate();
		return container;
	}

	public IAcceptor getJvmAcceptor(IManagedContainer container, String repoName) {
		return JVMUtil.getAcceptor(container, repoName);
	}

	public IConnector getJvmConnector(IManagedContainer container, String repoName) {
		return JVMUtil.getConnector(container, repoName);
	}

	public DataSource dataSource(File file, String repoName) throws Exception {
		// Don't use memory since the schema is written and a new data source is made
		final String dbUri = "jdbc:h2:" + file.toURI().toURL().toString(); //  getAbsolutePath();
		JdbcDataSource dataSource = new JdbcDataSource();
		dataSource.setUrl(dbUri);
		H2Adapter.createSchema(dataSource, repoName, true);
//		dataSource = new JdbcDataSource();
		dataSource.setURL(dbUri + ";SCHEMA=" + repoName);
		return dataSource;
	}

	public IDBAdapter h2Adapter() {
		return new H2Adapter();
	}

	public CdoServerImpl server(File tempFile, String repoName, boolean auditing, boolean branching, boolean withRanges,
			Map<String, String> repoProps) throws Exception {
		CdoServerImpl cdoServer = new CdoServerImpl();
		cdoServer.dbAdapter = h2Adapter();
		cdoServer.connectionProvider = cdoServer.dbAdapter.createConnectionProvider(dataSource(tempFile, repoName));
		cdoServer.container = serverContainer(true);
		cdoServer.acceptor = getJvmAcceptor(cdoServer.container, repoName);

		// XMI bundle needs to be on run path for internal CDO classes to work
		// Adding a '*' to the global resource factory registry doesn't seem to work
		cdoServer.activate(repoName, auditing, branching, withRanges, repoProps);
		return cdoServer;
	}
}
