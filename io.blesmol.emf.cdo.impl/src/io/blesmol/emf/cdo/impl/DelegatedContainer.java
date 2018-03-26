package io.blesmol.emf.cdo.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;

import org.eclipse.net4j.Net4jUtil;
import org.eclipse.net4j.jvm.JVMUtil;
import org.eclipse.net4j.tcp.TCPUtil;
import org.eclipse.net4j.tcp.ssl.SSLUtil;
import org.eclipse.net4j.util.container.ContainerUtil;
import org.eclipse.net4j.util.container.FactoryNotFoundException;
import org.eclipse.net4j.util.container.IElementProcessor;
import org.eclipse.net4j.util.container.IManagedContainer;
import org.eclipse.net4j.util.event.IListener;
import org.eclipse.net4j.util.factory.IFactory;
import org.eclipse.net4j.util.factory.IFactoryKey;
import org.eclipse.net4j.util.factory.ProductCreationException;
import org.eclipse.net4j.util.lifecycle.LifecycleException;
import org.eclipse.net4j.util.lifecycle.LifecycleState;
import org.eclipse.net4j.util.registry.IRegistry;

/**
 * Connectors and acceptors are responsible for preparing the container for
 * their use
 */
public class DelegatedContainer implements IManagedContainer {

	protected IManagedContainer delegate;

	protected void activate(String type) {

		delegate = ContainerUtil.createContainer();
		// Common for all containers
		Net4jUtil.prepareContainer(delegate);

		// Currently does not support 'http'
		switch (type) {
		case "jvm":
			JVMUtil.prepareContainer(delegate);
			break;
		case "ssl":
			SSLUtil.prepareContainer(delegate);
			break;
		case "tcp":
			TCPUtil.prepareContainer(delegate);
			break;
		default:
			throw new UnsupportedOperationException("Invalid Connector type: " + type);
		}
		activate();
	}

	@Override
	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	@Override
	public Object[] getElements() {
		return delegate.getElements();
	}

	@Override
	public void addListener(IListener listener) {
		delegate.addListener(listener);
	}

	@Override
	public void removeListener(IListener listener) {
		delegate.removeListener(listener);
	}

	@Override
	public boolean hasListeners() {
		return delegate.hasListeners();
	}

	@Override
	public IListener[] getListeners() {
		return delegate.getListeners();
	}

	@Override
	public void activate() throws LifecycleException {
		delegate.activate();
	}

	@Override
	public Exception deactivate() {
		return delegate.deactivate();
	}

	@Override
	public LifecycleState getLifecycleState() {
		return delegate.getLifecycleState();
	}

	@Override
	public boolean isActive() {
		return delegate.isActive();
	}

	@Override
	public IRegistry<IFactoryKey, IFactory> getFactoryRegistry() {
		return delegate.getFactoryRegistry();
	}

	@Override
	public IManagedContainer registerFactory(IFactory factory) {
		return delegate.registerFactory(factory);
	}

	@Override
	public List<IElementProcessor> getPostProcessors() {
		return delegate.getPostProcessors();
	}

	@Override
	public void addPostProcessor(IElementProcessor postProcessor, boolean processExistingElements) {
		delegate.addPostProcessor(postProcessor, processExistingElements);
	}

	@Override
	public void addPostProcessor(IElementProcessor postProcessor) {
		delegate.addPostProcessor(postProcessor);
	}

	@Override
	public void removePostProcessor(IElementProcessor postProcessor) {
		delegate.removePostProcessor(postProcessor);
	}

	@Override
	public Set<String> getProductGroups() {
		return delegate.getProductGroups();
	}

	@Override
	public Set<String> getFactoryTypes(String productGroup) {
		return delegate.getFactoryTypes(productGroup);
	}

	@Override
	public IFactory getFactory(String productGroup, String factoryType) throws FactoryNotFoundException {
		return delegate.getFactory(productGroup, factoryType);
	}

	@Override
	public Object putElement(String productGroup, String factoryType, String description, Object element) {
		return delegate.putElement(productGroup, factoryType, description, element);
	}

	@Override
	public String[] getElementKey(Object element) {
		return delegate.getElementKey(element);
	}

	@Override
	public Object[] getElements(String productGroup) {
		return delegate.getElements(productGroup);
	}

	@Override
	public Object[] getElements(String productGroup, String factoryType) {
		return delegate.getElements(productGroup, factoryType);
	}

	@Override
	public Object getElement(String productGroup, String factoryType, String description)
			throws FactoryNotFoundException, ProductCreationException {
		return delegate.getElement(productGroup, factoryType, description);
	}

	@Override
	public Object getElement(String productGroup, String factoryType, String description, boolean activate)
			throws FactoryNotFoundException, ProductCreationException {
		return delegate.getElement(productGroup, factoryType, description, activate);
	}

	@Override
	public Object removeElement(String productGroup, String factoryType, String description) {
		return delegate.removeElement(productGroup, factoryType, description);
	}

	@Override
	public void clearElements() {
		delegate.clearElements();
	}

	@Override
	public void loadElements(InputStream stream)
			throws IOException, FactoryNotFoundException, ProductCreationException {
		delegate.loadElements(stream);
	}

	@Override
	public void saveElements(OutputStream stream) throws IOException {
		delegate.saveElements(stream);
	}

}
