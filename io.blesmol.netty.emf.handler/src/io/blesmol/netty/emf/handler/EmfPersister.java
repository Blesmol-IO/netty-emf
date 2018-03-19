package io.blesmol.netty.emf.handler;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.util.concurrent.EventExecutorGroup;

public class EmfPersister {

	private static final Logger logger = LoggerFactory.getLogger(EmfPersister.class);

	protected EObject receiver;
	protected String featureName;
	protected EventExecutorGroup eventExecutorGroup;

	protected volatile EStructuralFeature feature;
	protected static final AtomicReferenceFieldUpdater<EmfPersister, EStructuralFeature> FEATURE_UPDATER = AtomicReferenceFieldUpdater
			.newUpdater(EmfPersister.class, EStructuralFeature.class, "feature");

	public void eSet(EObject eObject) {
		if (this.feature == null) {
			FEATURE_UPDATER.compareAndSet(this, null, receiver.eClass().getEStructuralFeature(featureName));
		}

		eventExecutorGroup.submit(() -> {
			receiver.eSet(this.feature, eObject);
			return null;
		}).addListener((f) -> {
			if (!f.isSuccess()) {
				logger.warn("Could not save EObject {} due to cause {}", eObject, f.cause());
			}
		});
	}

}
