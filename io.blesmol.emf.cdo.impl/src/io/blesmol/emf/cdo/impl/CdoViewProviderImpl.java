package io.blesmol.emf.cdo.impl;

import org.eclipse.emf.cdo.net4j.CDONet4jSession;
import org.eclipse.emf.cdo.net4j.CDONet4jSessionConfiguration;
import org.eclipse.emf.cdo.net4j.CDONet4jUtil;
import org.eclipse.emf.cdo.view.AbstractCDOViewProvider;
import org.eclipse.emf.cdo.view.CDOView;
import org.eclipse.emf.cdo.view.CDOViewProviderRegistry;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.net4j.connector.IConnector;
import org.eclipse.net4j.util.lifecycle.LifecycleUtil;

import io.blesmol.emf.cdo.api.CdoViewProvider;

public class CdoViewProviderImpl extends AbstractCDOViewProvider implements CdoViewProvider {

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
		final String repoName = getPath(uri);
		final CDONet4jSessionConfiguration config = CDONet4jUtil.createNet4jSessionConfiguration();
		config.setConnector(connector);
		config.setRepositoryName(repoName);
		final CDONet4jSession session = config.openNet4jSession();
		return session.openTransaction();
	}

	@Override
	public String getPath(URI uri) {
		if (uri.hasAbsolutePath()) {
			return uri.path().substring(1);
		} else if (uri.hasRelativePath()) {
			return uri.path();
		} else {
			throw new IllegalArgumentException("URI does not have absolute or relative path: " + uri.toString());
		}
	}
}
