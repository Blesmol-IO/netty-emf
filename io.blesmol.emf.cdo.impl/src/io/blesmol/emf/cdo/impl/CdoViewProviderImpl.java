package io.blesmol.emf.cdo.impl;

import org.eclipse.emf.cdo.net4j.CDONet4jSession;
import org.eclipse.emf.cdo.net4j.CDONet4jSessionConfiguration;
import org.eclipse.emf.cdo.net4j.CDONet4jUtil;
import org.eclipse.emf.cdo.util.CDOURIData;
import org.eclipse.emf.cdo.util.CDOURIUtil;
import org.eclipse.emf.cdo.view.AbstractCDOViewProvider;
import org.eclipse.emf.cdo.view.CDOView;
import org.eclipse.emf.cdo.view.CDOViewProviderRegistry;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.net4j.connector.IConnector;
import org.eclipse.net4j.util.lifecycle.LifecycleUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.blesmol.emf.cdo.api.CdoViewProvider;

public class CdoViewProviderImpl extends AbstractCDOViewProvider implements CdoViewProvider {

	private static Logger logger = LoggerFactory.getLogger(CdoViewProvider.class);

	/**
	 * A connector from a prepared, active container
	 */
	protected IConnector connector;

	/**
	 * Expected values must be set prior to calling activate
	 */
	protected void activate() {
		assert connector != null;
		assert getRegex() != null;
		assert getPriority() != -1;
		// Register
		CDOViewProviderRegistry.INSTANCE.addViewProvider(this);
	}

	protected void deactivate() {
		CDOViewProviderRegistry.INSTANCE.removeViewProvider(this);
		LifecycleUtil.deactivate(connector);
	}

	@Override
	public CDOView getView(URI uri, ResourceSet resourceSet) {
		logger.debug("Getting view for resource set {}, URI {}", resourceSet, uri);
		CDOURIData uriData = new CDOURIData(uri);
		final String repoName = uriData.getRepositoryName();
		assert repoName != null;
		final CDONet4jSessionConfiguration config = CDONet4jUtil.createNet4jSessionConfiguration();
		config.setConnector(connector);
		config.setRepositoryName(repoName);
		try {
			final CDONet4jSession session = config.openNet4jSession();
			logger.debug("Opening transaction on {} via URI {}", resourceSet, uriData);
			return session.openTransaction(resourceSet);
		} catch (Exception e) {
			logger.debug("Failed on opening transaction on resource set {}, URI {}, cause {}", resourceSet,
					uriData, e);
			return null;
		}

	}

}
