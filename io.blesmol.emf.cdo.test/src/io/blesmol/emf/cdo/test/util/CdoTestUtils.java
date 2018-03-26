package io.blesmol.emf.cdo.test.util;

import java.io.File;

import javax.sql.DataSource;

import org.eclipse.emf.cdo.eresource.impl.CDOResourceFactoryImpl;
import org.eclipse.emf.cdo.net4j.CDONet4jUtil;
import org.eclipse.emf.cdo.server.net4j.CDONet4jServerUtil;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceFactoryRegistryImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.net4j.Net4jUtil;
import org.eclipse.net4j.acceptor.IAcceptor;
import org.eclipse.net4j.connector.IConnector;
import org.eclipse.net4j.db.IDBAdapter;
import org.eclipse.net4j.db.h2.H2Adapter;
import org.eclipse.net4j.jvm.JVMUtil;
import org.eclipse.net4j.util.container.ContainerUtil;
import org.eclipse.net4j.util.container.IManagedContainer;
import org.h2.jdbcx.JdbcDataSource;

public class CdoTestUtils {

	public static final String SCHEMA_JVM = "cdo.net4j.jvm";

	public IManagedContainer serverContainer(boolean useJvm) {
		IManagedContainer container = ContainerUtil.createContainer();

		Net4jUtil.prepareContainer(container); // Register Net4j factories

		JVMUtil.prepareContainer(container); // Register JVM factories

		CDONet4jServerUtil.prepareContainer(container); // Register CDO server factories
		container.activate(); // standalone
		return container;
	}

	public IManagedContainer jvmClientContainer() {
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
		final String dbUri = "jdbc:h2:" + file.toURI().toURL().toString(); // getAbsolutePath();
		JdbcDataSource dataSource = new JdbcDataSource();
		dataSource.setUrl(dbUri);
		H2Adapter.createSchema(dataSource, repoName, true);
		// dataSource = new JdbcDataSource();
		dataSource.setURL(dbUri + ";SCHEMA=" + repoName);
		return dataSource;
	}

	public IDBAdapter h2Adapter() {
		return new H2Adapter();
	}

	public ResourceSet createAndPrepResourceSet(String scheme) {
		ResourceSet rs = new ResourceSetImpl();
		// Simulate CDO resource factory
		Resource.Factory.Registry reg = new ResourceFactoryRegistryImpl();
		reg.getProtocolToFactoryMap().put(scheme, new CDOResourceFactoryImpl());
		rs.setResourceFactoryRegistry(reg);
		return rs;
	}

}
