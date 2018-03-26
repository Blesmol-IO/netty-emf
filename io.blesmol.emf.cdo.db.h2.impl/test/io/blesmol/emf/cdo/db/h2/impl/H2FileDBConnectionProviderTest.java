package io.blesmol.emf.cdo.db.h2.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.eclipse.net4j.db.h2.H2Adapter;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import io.blesmol.emf.cdo.db.h2.api.H2Api;

public class H2FileDBConnectionProviderTest {
	
	private final String repoName = getClass().getSimpleName();

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Test
	public void shouldCreateFileCorrectly() throws Exception {
		File tempFile = tempFolder.newFile(repoName + H2Api.MV_STORE_SUFFIX);
		assertEquals(0, tempFile.length());
		String expectedFileUrl = tempFile.toURI().toURL().toString();
		String cleanFileUrl = expectedFileUrl.substring(0, expectedFileUrl.indexOf(H2Api.MV_STORE_SUFFIX));
		H2FileDbConnectionProvider provider = new H2FileDbConnectionProvider();
		File actualFile = provider.getFile(cleanFileUrl);
		assertEquals(expectedFileUrl, actualFile.toURI().toURL().toString());
	}
	
	@Test
	public void shouldCreateDb() throws Exception {
		H2FileDbConnectionProvider provider = new H2FileDbConnectionProvider();
		provider.dbAdapter = new H2Adapter();
		String repoName = "testRepo";
		File tempFile = tempFolder.newFile(repoName + H2Api.MV_STORE_SUFFIX);
		assertEquals(0, tempFile.length());
		String cleanFileUrl = tempFile.toURI().toURL().toString();
		cleanFileUrl = cleanFileUrl.substring(0, cleanFileUrl.indexOf(H2Api.MV_STORE_SUFFIX));
		provider.activate(cleanFileUrl);
		assertNotNull(provider.delegate);
		assertTrue(tempFile.length() > 0);
		// TODO: consider stronger test like verifying DB schema exists
	}

}
