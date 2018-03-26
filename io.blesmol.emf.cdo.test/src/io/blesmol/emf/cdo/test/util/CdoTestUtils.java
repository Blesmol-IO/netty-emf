package io.blesmol.emf.cdo.test.util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CdoTestUtils {

	protected static final Logger logger = LoggerFactory.getLogger(CdoTestUtils.class);

	public static final String SCHEMA_JVM = "cdo.net4j.jvm";
	public static final String H2_SUFFIX = ".mv.db";

	@Deprecated
	public IManagedContainer serverContainer(boolean useJvm) {
		IManagedContainer container = ContainerUtil.createContainer();

		Net4jUtil.prepareContainer(container); // Register Net4j factories

		JVMUtil.prepareContainer(container); // Register JVM factories

		CDONet4jServerUtil.prepareContainer(container); // Register CDO server factories
		container.activate(); // standalone
		return container;
	}

	@Deprecated
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

	public File repoFile(TemporaryFolder tempFolder, String repoName) throws Exception {
		String repoFileName = repoName + H2_SUFFIX;
		Path repoPath = Paths.get(tempFolder.getRoot().getAbsolutePath(), repoFileName);
		File repoFile;
		if (!Files.exists(repoPath)) {
			repoFile = tempFolder.newFile(repoFileName);
		} else {
			repoFile = repoPath.toFile();
		}
		return repoFile;
	}
	
	public DataSource dataSource(File file, String repoName) throws Exception {
		final String fileUrl = cleanH2FileUrl(file);
		final String dbUri = "jdbc:h2:" + fileUrl;
		final JdbcDataSource dataSource = new JdbcDataSource();
		if (file.length() == 0) {
			logger.debug("Creating new schema for dbUri {} and repository {}", dbUri, file.getAbsolutePath());
			dataSource.setUrl(dbUri);
			H2Adapter.createSchema(dataSource, repoName, true);
		}
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

	public String cleanH2FileUrl(File file) throws Exception {
		String results = file.toURI().toURL().toString();
		results = results.substring(0, results.indexOf(H2_SUFFIX));
		return results;
	}

	public IManagedContainer serverContainer(IManagedContainer container) {
		CDONet4jServerUtil.prepareContainer(container); // Register CDO server factories
		return container;
	}

	public IManagedContainer clientContainer(IManagedContainer container) {
		CDONet4jUtil.prepareContainer(container); // Register CDO client factories
		return container;
	}
	
	public IManagedContainer container(String type) {
		throw new UnsupportedOperationException("Needs to be implemented");
	}
}
