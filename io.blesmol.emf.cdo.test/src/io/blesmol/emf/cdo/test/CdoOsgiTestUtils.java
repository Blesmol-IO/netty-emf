package io.blesmol.emf.cdo.test;

import java.io.File;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;

import io.blesmol.emf.cdo.api.CdoApi;

public class CdoOsgiTestUtils {

	public static final String H2_SUFFIX = ".mv.db";

	public static final String TARGET_KEY = "blesmol.test";
	
	public String cleanH2FileUrl(File file) throws Exception {
		String results = file.toURI().toURL().toString();
		results = results.substring(0, results.indexOf(H2_SUFFIX));
		return results;
	}

	public void putMinimalProperties(Map<String, Object> properties, String repoName, String description, String type, String fileUrl) {
		properties.put("emf.cdo.connector.description", description);
		properties.put("emf.cdo.acceptor.description", description);
		// Is this correct? Description for TCP will be host:port
		properties.put("blesmol.cdoserver.reponame", repoName);
		properties.put("emf.cdo.managedcontainer.type", type);
		properties.put("emf.cdo.connector.type", type);
		properties.put("emf.cdo.acceptor.type", type);
		properties.put("emf.cdo.connectionprovider.url", fileUrl);
	}

	public void putEmfProperties(Map<String, Object> properties, String uri) {
		properties.put("emf.uri", uri);
//		properties.put(EmfApi.SCHEME, new String[]{"cdo", "cdo.net4j.jvm", "cdo.net4j.tcp", "cdo.net4j.ssl"});

	}

	public void putTargets(Map<String, Object> properties, String targetValue) {
		final String suffix = ".target";
		final String target = String.format("(%s=%s)", TARGET_KEY, targetValue);
		properties.put(TARGET_KEY, targetValue);
		properties.put(CdoApi.IDBConnectionProvider.Reference.DB_ADAPTER + suffix, target);
		properties.put(CdoApi.IAcceptor.Reference.MANAGED_CONTAINER + suffix, target);
		properties.put(CdoApi.IConnector.Reference.MANAGED_CONTAINER + suffix, target);
		properties.put(CdoApi.CdoServer.Reference.MANAGED_CONTAINER + suffix, target);
		properties.put(CdoApi.CdoServer.Reference.ACCEPTOR + suffix, target);
		properties.put(CdoApi.CdoServer.Reference.DB_ADAPTER + suffix, target);
		properties.put(CdoApi.CdoServer.Reference.DB_CONNECTION_PROVIDER + suffix, target);
	}

	public static class LatchServiceListener implements ServiceListener {

		private final CountDownLatch latch;
		private final String sourceName;

		public LatchServiceListener(CountDownLatch latch, String sourceName) {
			super();
			this.latch = latch;
			this.sourceName = sourceName;
		}

		@Override
		public void serviceChanged(ServiceEvent event) {
			if (event.getType() != ServiceEvent.REGISTERED)
				return;

			// Hack-y way, and probably implementation-specific way on filtering for source
			// references.
			// Note: obtaining the service reference and getting the service causes unusual
			// behavior, so don't do that :)
			if (event.getSource().toString().contains(sourceName)) {
				latch.countDown();
			}
		}

	}
}
