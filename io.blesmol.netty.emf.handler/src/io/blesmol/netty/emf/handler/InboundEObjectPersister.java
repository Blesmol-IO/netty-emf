package io.blesmol.netty.emf.handler;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.EventExecutorGroup;

/**
 * A sharable inbound handler that save messages to some EObject
 */
@Sharable
public class InboundEObjectPersister extends ChannelInboundHandlerAdapter {

	private static final Logger logger = LoggerFactory.getLogger(InboundEObjectPersister.class);

	protected EObject receiver;
	protected String eStructuralFeatureName;
	protected EventExecutorGroup eventExecutorGroup;

	protected volatile EStructuralFeature feature;
	protected static final AtomicReferenceFieldUpdater<InboundEObjectPersister, EStructuralFeature> FEATURE_UPDATER = AtomicReferenceFieldUpdater.newUpdater(InboundEObjectPersister.class, EStructuralFeature.class, "feature");
	
	//
	// The setter / unsetters below are to be only called after ctor but before
	// the service methods.
	//

	protected void setEObject(EObject eObject) {
		this.receiver = eObject;
	}

	protected void unsetEObject(EObject eObject) {
		this.receiver = null;
	}

	protected void setEStructuralFeatureName(String eStructuralFeatureName) {
		this.eStructuralFeatureName = eStructuralFeatureName;
	}

	protected void unsetEStructuralFeatureName(String eStructuralFeatureName) {
		this.eStructuralFeatureName = null;
	}

	protected void setEventExecutorGroup(EventExecutorGroup eventExecutorGroup) {
		this.eventExecutorGroup = eventExecutorGroup;
	}

	protected void unsetEventExecutorGroup(EventExecutorGroup eventExecutorGroup) {
		this.eventExecutorGroup = null;
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof EObject) {
			eventExecutorGroup.submit(setMessage((EObject)msg)).addListener((f) -> {
				if (!f.isSuccess()) {
					logger.warn("Could not save EObject {} due to cause {}", msg, f.cause());
				}
			});
		}
		ReferenceCountUtil.retain(msg);
		ctx.fireChannelRead(msg);
	}

	Callable<Void> setMessage(EObject eObject) {
		if (this.feature == null) {
			FEATURE_UPDATER.compareAndSet(this, null, receiver.eClass().getEStructuralFeature(eStructuralFeatureName));
		}
		return () -> {
			receiver.eSet(this.feature, eObject);
			return null;
		};
	}

	@Override
	public boolean isSharable() {
		return true;
	}
}
