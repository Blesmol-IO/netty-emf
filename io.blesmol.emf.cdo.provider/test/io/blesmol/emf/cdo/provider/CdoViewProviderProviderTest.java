package io.blesmol.emf.cdo.provider;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
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
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.net4j.db.IDBAdapter;
import org.eclipse.net4j.util.container.IManagedContainer;
import org.eclipse.net4j.util.lifecycle.LifecycleUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import io.blesmol.emf.cdo.api.CdoApi;
import io.blesmol.emf.cdo.impl.ImplCdoTestUtils;
import io.blesmol.emf.test.util.EmfTestUtils;

public class CdoViewProviderProviderTest {

	private EmfTestUtils emfTestUtils = new EmfTestUtils();
	private ImplCdoTestUtils cdoTestUtils = new ImplCdoTestUtils();

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	/**
	 * FIXME: most of this is copy pasta
	 * 
	 * @see io.blesmol.emf.cdo.impl.CdoViewProviderImplITest#shouldSaveGetEObjectTransparentlyViaViewProvider() 
	 */
	@Test
	public void shouldSaveGetEObjectTransparentlyViaViewProvider() throws Exception {
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
		when(viewConfig.blesmol_cdoviewprovider_regex()).thenReturn(CdoApi.CdoViewProvider.REGEX);
		when(viewConfig.blesmol_cdoviewprovider_priority()).thenReturn(1000);

		CdoViewProviderProvider viewProvider = new CdoViewProviderProvider();
		viewProvider.setContainer(container);
		viewProvider.setRegex(CdoApi.CdoViewProvider.REGEX);
		viewProvider.setPriority(1000);
		viewProvider.activate(viewConfig, Collections.emptyMap());

		final String resourceName = "/test";
		URI uri = URI.createURI(
				String.format("cdo.net4j.jvm://%s/%s/%s?transactional=true", repoName, repoName, resourceName));
		ResourceSet rs = cdoTestUtils.createAndPrepResourceSet(ImplCdoTestUtils.SCHEMA_JVM);
		Resource resource = rs.createResource(uri);
		assertNotNull(resource);

		final EObject expectedEObject = emfTestUtils.eObject("TestObject", "testAttribute",
				EcorePackage.Literals.ESTRING, "TestPackage", "testPackage", "test://someTest/package");
		resource.getContents().add(expectedEObject);
		resource.save(null);

		rs = null;
		resource = null;
		rs = cdoTestUtils.createAndPrepResourceSet(ImplCdoTestUtils.SCHEMA_JVM);
		resource = rs.getResource(uri, true);
		Object object = resource.getContents().get(0);
		assertTrue(object instanceof EObject);
		emfTestUtils.assertEObjects(expectedEObject, (EObject) object);

		// viewProvider.setContainer(container);
		// viewProvider.activate(viewConfig, Collections.emptyMap());
		//
		// final URI uri = URI.createURI("cdo.net4j.jvm://%s/%s/" + repoName);
		// final ResourceSet rs =
		// cdoTestUtils.createAndPrepResourceSet(CdoTestUtils.SCHEMA_JVM);
		// Resource resource = rs.createResource(uri);
		// assertNotNull(resource);
		//
		// String expectedClassName = "TestObject";
		// String expectedAttrName = "testAttribute";
		// String expectedPackageName = "TestPackage";
		// String expectedNsPrefix = "testPackage";
		// String expectedNsUri = "test://someTest/package";
		//
		// final EObject expectedEObject = emfTestUtils.eObject(expectedClassName,
		// expectedAttrName,
		// EcorePackage.Literals.ESTRING, expectedPackageName, expectedNsPrefix,
		// expectedNsUri);
		// resource.getContents().add(expectedEObject);
		// resource.save(null);
		//
		//
		// // Clean up
		// LifecycleUtil.deactivate(tx);
		LifecycleUtil.deactivate(viewProvider);
		serverProvider.deactivate();

	}

}
