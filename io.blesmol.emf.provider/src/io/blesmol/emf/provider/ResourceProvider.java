package io.blesmol.emf.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import io.blesmol.emf.api.EmfApi;

@Component(configurationPid = EmfApi.Resource.PID, configurationPolicy = ConfigurationPolicy.REQUIRE, service = Resource.class, immediate=true)
public class ResourceProvider implements Resource {

	Resource delegate;

	@Reference
	LogService logger;

	@Reference(name = EmfApi.Resource.Reference.RESOURCE_SET)
	ResourceSet resourceSet;

	@Activate
	void activate(EmfApi.Resource config) {
		// TODO: consider calling via an executor versus the activate thread
		createOrGetResource(config.emf_uri());
	}

	void createOrGetResource(String rawUri) {
		final URI uri = URI.createURI(rawUri);
		boolean _loadOnDemand = true;

		if (_loadOnDemand) {
			try {
				delegate = resourceSet.getResource(uri, true);
				logger.log(LogService.LOG_DEBUG, String.format("Obtained a resource via the URI %s.", uri.toString()));
			} catch (Exception e) {
				_loadOnDemand = false;
			}
		}
		if (!_loadOnDemand) {
			delegate = resourceSet.getResource(uri, false);
			if (delegate != null) {
				logger.log(LogService.LOG_DEBUG, String.format("Obtained a resource via the URI %s.", uri.toString()));
			}
		}
		if (delegate == null) {
			RuntimeException e = new IllegalStateException(
					String.format("Was not able to get resource via URI %s", uri.toString()));
			logger.log(LogService.LOG_ERROR, "", e);
			throw e;
		}
	}

	@Override
	public EList<Adapter> eAdapters() {
		return delegate.eAdapters();
	}

	@Override
	public boolean eDeliver() {
		return delegate.eDeliver();
	}

	@Override
	public void eSetDeliver(boolean deliver) {
		delegate.eSetDeliver(deliver);
	}

	@Override
	public void eNotify(Notification notification) {
		delegate.eNotify(notification);
	}

	@Override
	public ResourceSet getResourceSet() {
		return delegate.getResourceSet();
	}

	@Override
	public URI getURI() {
		return delegate.getURI();
	}

	@Override
	public void setURI(URI uri) {
		delegate.setURI(uri);
	}

	@Override
	public long getTimeStamp() {
		return delegate.getTimeStamp();
	}

	@Override
	public void setTimeStamp(long timeStamp) {
		delegate.setTimeStamp(timeStamp);
	}

	@Override
	public EList<EObject> getContents() {
		return delegate.getContents();
	}

	@Override
	public TreeIterator<EObject> getAllContents() {
		return delegate.getAllContents();
	}

	@Override
	public String getURIFragment(EObject eObject) {
		return delegate.getURIFragment(eObject);
	}

	@Override
	public EObject getEObject(String uriFragment) {
		return delegate.getEObject(uriFragment);
	}

	@Override
	public void save(Map<?, ?> options) throws IOException {
		delegate.save(options);
	}

	@Override
	public void load(Map<?, ?> options) throws IOException {
		delegate.load(options);
	}

	@Override
	public void save(OutputStream outputStream, Map<?, ?> options) throws IOException {
		delegate.save(outputStream, options);
	}

	@Override
	public void load(InputStream inputStream, Map<?, ?> options) throws IOException {
		delegate.load(inputStream, options);
	}

	@Override
	public boolean isTrackingModification() {
		return delegate.isTrackingModification();
	}

	@Override
	public void setTrackingModification(boolean isTrackingModification) {
		delegate.setTrackingModification(isTrackingModification);
	}

	@Override
	public boolean isModified() {
		return delegate.isModified();
	}

	@Override
	public void setModified(boolean isModified) {
		delegate.setModified(isModified);
	}

	@Override
	public boolean isLoaded() {
		return delegate.isLoaded();
	}

	@Override
	public void unload() {
		delegate.unload();
	}

	@Override
	public void delete(Map<?, ?> options) throws IOException {
		delegate.delete(options);
	}

	@Override
	public EList<Diagnostic> getErrors() {
		return delegate.getErrors();
	}

	@Override
	public EList<Diagnostic> getWarnings() {
		return delegate.getWarnings();
	}

}
