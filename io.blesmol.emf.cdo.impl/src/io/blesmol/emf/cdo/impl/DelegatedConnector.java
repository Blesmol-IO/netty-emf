package io.blesmol.emf.cdo.impl;

import java.util.Collection;

import org.eclipse.emf.cdo.net4j.CDONet4jUtil;
import org.eclipse.net4j.channel.ChannelException;
import org.eclipse.net4j.channel.IChannel;
import org.eclipse.net4j.connector.ConnectorException;
import org.eclipse.net4j.connector.ConnectorState;
import org.eclipse.net4j.connector.IConnector;
import org.eclipse.net4j.protocol.IProtocol;
import org.eclipse.net4j.util.container.IManagedContainer;
import org.eclipse.net4j.util.event.IListener;
import org.eclipse.net4j.util.lifecycle.LifecycleUtil;

@Deprecated
public class DelegatedConnector implements IConnector {

	protected IManagedContainer container;
	// protected String type;
	// protected String description;
	// protected String productGroup;

	protected IConnector delegate;

	protected void activate(String productGroup, String type, String description) {

		// Common factories for clients / connectors
		CDONet4jUtil.prepareContainer(container);

		// Currently does not support 'http'
		switch (type) {
		case "jvm":
		case "ssl":
		case "tcp":
			break;
		default:
			throw new UnsupportedOperationException("Invalid Connector type: " + type);
		}

		delegate = (IConnector) container.getElement(productGroup, type, description);
		assert delegate != null;
	}

	protected void deactivate() {
		// Don't deactivate container here, since it could be used by
		// other consumers
		// LifecycleUtil.deactivate(container);
		LifecycleUtil.deactivate(this);
	}

	@Override
	public IChannel openChannel() throws ChannelException {
		return delegate.openChannel();
	}

	@Override
	public IChannel openChannel(String protocolID, Object infraStructure) throws ChannelException {
		return delegate.openChannel(protocolID, infraStructure);
	}

	@Override
	public IChannel openChannel(IProtocol<?> protocol) throws ChannelException {
		return delegate.openChannel(protocol);
	}

	@Override
	public Collection<IChannel> getChannels() {
		return delegate.getChannels();
	}

	@Override
	public long getOpenChannelTimeout() {
		return delegate.getOpenChannelTimeout();
	}

	@Override
	public void setOpenChannelTimeout(long openChannelTimeout) {
		delegate.setOpenChannelTimeout(openChannelTimeout);
	}

	@Override
	public Location getLocation() {
		return delegate.getLocation();
	}

	@Override
	public boolean isClient() {
		return delegate.isClient();
	}

	@Override
	public boolean isServer() {
		return delegate.isServer();
	}

	@Override
	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	@Override
	public IChannel[] getElements() {
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
	public String getUserID() {
		return delegate.getUserID();
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
	public String getURL() {
		return delegate.getURL();
	}

	@Override
	public ConnectorState getState() {
		return delegate.getState();
	}

	@Override
	public boolean isConnected() {
		return delegate.isConnected();
	}

	@Override
	public void connect() throws ConnectorException {
		delegate.connect();
	}

	@Override
	public void connect(long timeout) throws ConnectorException {
		delegate.connect(timeout);
	}

	@Override
	public void connectAsync() throws ConnectorException {
		delegate.connectAsync();
	}

	@Override
	public void waitForConnection(long timeout) throws ConnectorException {
		delegate.waitForConnection(timeout);
	}

}
