package io.blesmol.netty.notify.impl;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.notify.impl.NotificationChainImpl;
import org.eclipse.emf.common.notify.impl.NotificationImpl;
import org.eclipse.emf.common.util.EList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundInvoker;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelProgressivePromise;
import io.netty.channel.ChannelPromise;
import io.netty.util.concurrent.EventExecutorGroup;

/**
 * 
 * 
 * We have no way of knowing if the pipeline.names() list was "set" so for all
 * notifications assume it's not set
 * 
 */
public class NotifyingChannelPipeline implements Notifier, ChannelPipeline {

	protected static final Logger logger = LoggerFactory.getLogger(NotifyingChannelPipeline.class);
	/**
	 * The names feature
	 * {@link org.eclipse.emf.common.notify.Notification#getFeatureID ID}.
	 */
	public static final int CHANNEL_PIPELINE__NAMES = 0;

	/**
	 * Set prior to calling {@link #activate()}
	 */
	protected volatile ChannelPipeline delegatingPipeline;

	protected final Notifier delegatedNotifier = new AtomicNotifierImpl();

	protected void activate() {
		assert delegatingPipeline != null;
	}

	protected void deactivate() {
		delegatingPipeline.close();
		delegatingPipeline = null;
		delegatedNotifier.eAdapters().clear();
	}

	@Override
	public EList<Adapter> eAdapters() {
		return delegatedNotifier.eAdapters();
	}

	@Override
	public boolean eDeliver() {
		return delegatedNotifier.eDeliver();
	}

	@Override
	public void eSetDeliver(boolean deliver) {
		delegatedNotifier.eSetDeliver(deliver);
	}

	@Override
	public void eNotify(Notification notification) {
		delegatedNotifier.eNotify(notification);
	}

	@Override
	public ChannelFuture bind(SocketAddress localAddress) {
		return delegatingPipeline.bind(localAddress);
	}

	@Override
	public ChannelFuture connect(SocketAddress remoteAddress) {
		return delegatingPipeline.connect(remoteAddress);
	}

	@Override
	public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress) {
		return delegatingPipeline.connect(remoteAddress, localAddress);
	}

	@Override
	public ChannelFuture disconnect() {
		return delegatingPipeline.disconnect();
	}

	@Override
	public ChannelFuture close() {
		return delegatingPipeline.close();
	}

	@Override
	public ChannelFuture deregister() {
		return delegatingPipeline.deregister();
	}

	@Override
	public ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise) {
		return delegatingPipeline.bind(localAddress, promise);
	}

	@Override
	public ChannelFuture connect(SocketAddress remoteAddress, ChannelPromise promise) {
		return delegatingPipeline.connect(remoteAddress, promise);
	}

	@Override
	public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
		return delegatingPipeline.connect(remoteAddress, localAddress, promise);
	}

	@Override
	public ChannelFuture disconnect(ChannelPromise promise) {
		return delegatingPipeline.disconnect(promise);
	}

	@Override
	public ChannelFuture close(ChannelPromise promise) {
		return delegatingPipeline.close(promise);
	}

	@Override
	public ChannelFuture deregister(ChannelPromise promise) {
		return delegatingPipeline.deregister(promise);
	}

	@Override
	public ChannelOutboundInvoker read() {
		return delegatingPipeline.read();
	}

	@Override
	public ChannelFuture write(Object msg) {
		return delegatingPipeline.write(msg);
	}

	@Override
	public ChannelFuture write(Object msg, ChannelPromise promise) {
		return delegatingPipeline.write(msg, promise);
	}

	@Override
	public ChannelFuture writeAndFlush(Object msg, ChannelPromise promise) {
		return delegatingPipeline.writeAndFlush(msg, promise);
	}

	@Override
	public ChannelFuture writeAndFlush(Object msg) {
		return delegatingPipeline.writeAndFlush(msg);
	}

	@Override
	public ChannelPromise newPromise() {
		return delegatingPipeline.newPromise();
	}

	@Override
	public ChannelProgressivePromise newProgressivePromise() {
		return delegatingPipeline.newProgressivePromise();
	}

	@Override
	public ChannelFuture newSucceededFuture() {
		return delegatingPipeline.newSucceededFuture();
	}

	@Override
	public ChannelFuture newFailedFuture(Throwable cause) {
		return delegatingPipeline.newFailedFuture(cause);
	}

	@Override
	public ChannelPromise voidPromise() {
		return delegatingPipeline.voidPromise();
	}

	@Override
	public Iterator<Entry<String, ChannelHandler>> iterator() {
		return delegatingPipeline.iterator();
	}

	// NOTIFY START

	protected boolean isNamesSet() {
		return !names().isEmpty();
	}

	protected NotificationImpl createNotification(int eventType, Object oldObject, Object newObject, int index,
			boolean wasSet) {

		return new NotificationImpl(eventType, oldObject, newObject, index, wasSet) {

			@Override
			public Object getNotifier() {
				return NotifyingChannelPipeline.this;
			}

			@Override
			public int getFeatureID(Class<?> expectedClass) {
				return CHANNEL_PIPELINE__NAMES;
			}
		};
	}
	
	@Override
	public ChannelPipeline addAfter(String baseName, String name, ChannelHandler handler) {
		return addAfter(null, baseName, name, handler);
	}

	@Override
	public ChannelPipeline addAfter(EventExecutorGroup group, String baseName, String name, ChannelHandler handler) {
		boolean oldIsSet = isNamesSet();
		delegatingPipeline.addAfter(group, baseName, name, handler);
		final List<String> afterNames = names();
		final int index = afterNames.indexOf(baseName) + 1;
		final String addName = afterNames.get(index);
		final Notification notification = createNotification(Notification.ADD, null, addName, index, oldIsSet);
		eNotify(notification);
		return this;
	}

	@Override
	public ChannelPipeline addBefore(String baseName, String name, ChannelHandler handler) {
		return addBefore(null, baseName, name, handler);
	}

	@Override
	public ChannelPipeline addBefore(EventExecutorGroup group, String baseName, String name, ChannelHandler handler) {
		boolean oldIsSet = isNamesSet();
		delegatingPipeline.addBefore(group, baseName, name, handler);
		final List<String> afterNames = names();
		final int index = afterNames.indexOf(baseName) - 1;
		final String addName = afterNames.get(index);
		final Notification notification = createNotification(Notification.ADD, null, addName, index, oldIsSet);
		eNotify(notification);
		return this;
	}

	@Override
	public ChannelPipeline addFirst(ChannelHandler... handlers) {
		return addFirst(null, handlers);
	}

	// Super delegates to addFirst(EventExecutorGroup, String, ChannelHandler)
	// BUG: So as not to duplicate events, we ignore the ADD_MANY event here
	@Override
	public ChannelPipeline addFirst(EventExecutorGroup group, ChannelHandler... handlers) {
		return delegatingPipeline.addFirst(group, handlers);
	}

	@Override
	public ChannelPipeline addFirst(String name, ChannelHandler handler) {
		return addFirst(null, name, handler);
	}

	@Override
	public ChannelPipeline addFirst(EventExecutorGroup group, String name, ChannelHandler handler) {
		boolean oldIsSet = isNamesSet();
		delegatingPipeline.addFirst(group, name, handler);
		final List<String> afterNames = names();
		final int index = 0;
		final String addName = afterNames.get(index);
		final Notification notification = createNotification(Notification.ADD, null, addName, index, oldIsSet);
		eNotify(notification);
		return this;
	}

	@Override
	public ChannelPipeline addLast(ChannelHandler... handlers) {
		return addLast(null, handlers);
	}

	@Override
	public ChannelPipeline addLast(EventExecutorGroup group, ChannelHandler... handlers) {
		return delegatingPipeline.addLast(group, handlers);
	}

	@Override
	public ChannelPipeline addLast(String name, ChannelHandler handler) {
		return addLast(null, name, handler);
	}

	@Override
	public ChannelPipeline addLast(EventExecutorGroup group, String name, ChannelHandler handler) {
		boolean oldIsSet = isNamesSet();
		delegatingPipeline.addLast(group, name, handler);
		final List<String> afterNames = names();
		final int index = names().size() - 1;
		final String addName = afterNames.get(index);
		final Notification notification = createNotification(Notification.ADD, null, addName, index, oldIsSet);
		eNotify(notification);
		return this;
	}

	protected final static String LIST_SIZE_NOT = "Expected only {} removed item remaining in the list; acutal: {}";

	protected void removeNotify(List<String> beforeNames) {
		final List<String> afterNames = names();
		beforeNames.removeAll(afterNames);
		if (beforeNames.size() != 1) {
			logger.warn(LIST_SIZE_NOT, 1, beforeNames);
		} else {
			final String beforeName = beforeNames.get(0);
			final int index = beforeNames.indexOf(beforeName);
			final Notification notification = createNotification(Notification.REMOVE, beforeName, null, index, false);
			eNotify(notification);
		}
	}

	@Override
	public ChannelPipeline remove(ChannelHandler handler) {
		final List<String> beforeNames = names();
		delegatingPipeline.remove(handler);
		removeNotify(beforeNames);
		return this;
	}

	@Override
	public ChannelHandler remove(String name) {
		final List<String> beforeNames = names();
		final ChannelHandler removedHandler = delegatingPipeline.remove(name);
		removeNotify(beforeNames);
		return removedHandler;
	}

	@Override
	public <T extends ChannelHandler> T remove(Class<T> handlerType) {
		final List<String> beforeNames = names();
		final T removedHandler = delegatingPipeline.remove(handlerType);
		removeNotify(beforeNames);
		return removedHandler;
	}

	@Override
	public ChannelHandler removeFirst() {
		final List<String> beforeNames = names();
		final ChannelHandler removedHandler = delegatingPipeline.removeFirst();
		removeNotify(beforeNames);
		return removedHandler;
	}

	@Override
	public ChannelHandler removeLast() {
		final List<String> beforeNames = names();
		final ChannelHandler removedHandler = delegatingPipeline.removeLast();
		removeNotify(beforeNames);
		return removedHandler;
	}

	protected void replaceNotify(List<String> beforeNames) {
		final List<String> afterNames = names();
		final List<String> modifiedBeforeNames = new ArrayList<String>(beforeNames);
		modifiedBeforeNames.removeAll(afterNames);
		if (modifiedBeforeNames.size() == 1) {
			final String beforeName = modifiedBeforeNames.get(0);
			final int index = beforeNames.indexOf(beforeName);
			final String afterName = afterNames.get(index);
			NotificationChain replaceChain = new NotificationChainImpl(2);
			replaceChain.add(createNotification(Notification.REMOVE, beforeName, null, index, false));
			replaceChain.add(createNotification(Notification.ADD, null, afterName, index, !afterNames.isEmpty()));
			replaceChain.dispatch();
		} else {
			logger.warn(LIST_SIZE_NOT, 1, modifiedBeforeNames);
		}
	}
	
	// newName can be null
	@Override
	public ChannelPipeline replace(ChannelHandler oldHandler, String newName, ChannelHandler newHandler) {
		final List<String> beforeNames = names();
		delegatingPipeline.replace(oldHandler, newName, newHandler);
		replaceNotify(beforeNames);
		return this;
	}

	@Override
	public ChannelHandler replace(String oldName, String newName, ChannelHandler newHandler) {
		final List<String> beforeNames = names();
		final ChannelHandler handler = delegatingPipeline.replace(oldName, newName, newHandler);
		replaceNotify(beforeNames);
		return handler;
		
	}

	@Override
	public <T extends ChannelHandler> T replace(Class<T> oldHandlerType, String newName, ChannelHandler newHandler) {
		final List<String> beforeNames = names();
		final T handler = delegatingPipeline.replace(oldHandlerType, newName, newHandler);
		replaceNotify(beforeNames);
		return handler;

	}
	// NOTIFY END

	@Override
	public ChannelHandler first() {
		return delegatingPipeline.first();
	}

	@Override
	public ChannelHandlerContext firstContext() {
		return delegatingPipeline.firstContext();
	}

	@Override
	public ChannelHandler last() {
		return delegatingPipeline.last();
	}

	@Override
	public ChannelHandlerContext lastContext() {
		return delegatingPipeline.lastContext();
	}

	@Override
	public ChannelHandler get(String name) {
		return delegatingPipeline.get(name);
	}

	@Override
	public <T extends ChannelHandler> T get(Class<T> handlerType) {
		return delegatingPipeline.get(handlerType);
	}

	@Override
	public ChannelHandlerContext context(ChannelHandler handler) {
		return delegatingPipeline.context(handler);
	}

	@Override
	public ChannelHandlerContext context(String name) {
		return delegatingPipeline.context(name);
	}

	@Override
	public ChannelHandlerContext context(Class<? extends ChannelHandler> handlerType) {
		return delegatingPipeline.context(handlerType);
	}

	@Override
	public Channel channel() {
		return delegatingPipeline.channel();
	}

	@Override
	public List<String> names() {
		return delegatingPipeline.names();
	}

	@Override
	public Map<String, ChannelHandler> toMap() {
		return delegatingPipeline.toMap();
	}

	@Override
	public ChannelPipeline fireChannelRegistered() {
		return delegatingPipeline.fireChannelRegistered();
	}

	@Override
	public ChannelPipeline fireChannelUnregistered() {
		return delegatingPipeline.fireChannelUnregistered();
	}

	@Override
	public ChannelPipeline fireChannelActive() {
		return delegatingPipeline.fireChannelActive();
	}

	@Override
	public ChannelPipeline fireChannelInactive() {
		return delegatingPipeline.fireChannelActive();
	}

	@Override
	public ChannelPipeline fireExceptionCaught(Throwable cause) {
		return delegatingPipeline.fireExceptionCaught(cause);
	}

	@Override
	public ChannelPipeline fireUserEventTriggered(Object event) {
		return delegatingPipeline.fireUserEventTriggered(event);
	}

	@Override
	public ChannelPipeline fireChannelRead(Object msg) {
		return delegatingPipeline.fireChannelRead(msg);
	}

	@Override
	public ChannelPipeline fireChannelReadComplete() {
		return delegatingPipeline.fireChannelReadComplete();
	}

	@Override
	public ChannelPipeline fireChannelWritabilityChanged() {
		return delegatingPipeline.fireChannelWritabilityChanged();
	}

	@Override
	public ChannelPipeline flush() {
		return delegatingPipeline.flush();
	}

}
