package io.blesmol.emf.cdo.impl;

import org.eclipse.emf.cdo.server.net4j.CDONet4jServerUtil;
import org.eclipse.net4j.acceptor.IAcceptor;
import org.eclipse.net4j.connector.IConnector;
import org.eclipse.net4j.jvm.JVMUtil;
import org.eclipse.net4j.tcp.TCPUtil;
import org.eclipse.net4j.tcp.ssl.SSLUtil;
import org.eclipse.net4j.util.container.IManagedContainer;
import org.eclipse.net4j.util.event.IListener;

public class DelegatedAcceptor implements IAcceptor {

	protected IManagedContainer container;

	protected IAcceptor delegate;

	protected void activate(String type, String description) {
		assert container != null;

		// Common factories for servers / acceptors, adds "cdo" protocol
		CDONet4jServerUtil.prepareContainer(container);

		// Currently does not support 'http'
		switch (type) {
		case "jvm":
			delegate = JVMUtil.getAcceptor(container, description);
			break;
		case "ssl":
			delegate = SSLUtil.getAcceptor(container, description);
			break;
		case "tcp":
			delegate = TCPUtil.getAcceptor(container, description);
			break;
		default:
			throw new UnsupportedOperationException("Invalid Connector type: " + type);
		}

		assert delegate != null;
	}

	protected void deactivate() {
		delegate.close();
		// Don't deactivate container here, since it could be used by
		// other consumers
		// container.deactivate();
	}

	@Override
	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	@Override
	public IConnector[] getElements() {
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
	public void close() {
		delegate.close();
	}

	@Override
	public boolean isClosed() {
		return delegate.isClosed();
	}

	@Override
	public IConnector[] getAcceptedConnectors() {
		return delegate.getAcceptedConnectors();
	}

}
