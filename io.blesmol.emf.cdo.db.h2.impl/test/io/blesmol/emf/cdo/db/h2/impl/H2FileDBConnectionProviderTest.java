package io.blesmol.emf.cdo.db.h2.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.eclipse.net4j.db.h2.H2Adapter;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class H2FileDBConnectionProviderTest {

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Test
	public void shouldCreateDb() throws Exception {
		H2FileDbConnectionProvider provider = new H2FileDbConnectionProvider();
		provider.dbAdapter = new H2Adapter();
		String repoName = "testRepo";
		File tempFile = tempFolder.newFile(repoName + ".mv.db");
		assertEquals(0, tempFile.length());
		String cleanFileUrl = tempFile.toURI().toURL().toString();
		cleanFileUrl = cleanFileUrl.substring(0, cleanFileUrl.indexOf(".mv.db"));
		provider.activate(cleanFileUrl);
		assertNotNull(provider.delegate);
		assertTrue(tempFile.length() > 0);
		// TODO: consider stronger test like verifying DB schema exists
	}

}
