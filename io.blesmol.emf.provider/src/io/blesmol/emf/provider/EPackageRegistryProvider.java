package io.blesmol.emf.provider;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EPackage;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.log.LogService;

import io.blesmol.emf.api.EmfApi;

@Component(configurationPid = EmfApi.EPackage_Registry.PID, configurationPolicy=ConfigurationPolicy.REQUIRE, service = EPackage.Registry.class)
public class EPackageRegistryProvider implements EPackage.Registry {

	private final Map<String, Object> delegate = new ConcurrentHashMap<>();

	@Reference
	LogService logger;

	@Reference(name = EmfApi.EPackage_Registry.Reference.EPACKAGES, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MULTIPLE)
	void setEPackages(EPackage ePackage, Map<String, Object> properties) {
		getUri(ePackage, properties).ifPresent(nsUri -> put(nsUri, ePackage));
	}

	void unsetEPackages(EPackage ePackage, Map<String, Object> properties) {
		getUri(ePackage, properties).ifPresent(nsUri -> remove(nsUri, ePackage));
	}

	@Reference(name = EmfApi.EPackage_Registry.Reference.EPACKAGE_DESCRIPTORS, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MULTIPLE)
	void setEPackageDescriptors(EPackage.Descriptor descriptor, Map<String, Object> properties) {
		getUri(descriptor, properties).ifPresent(nsUri -> put(nsUri, descriptor));
	}

	void unsetEPackageDescriptors(EPackage.Descriptor descriptor, Map<String, Object> properties) {
		getUri(descriptor, properties).ifPresent(nsUri -> remove(nsUri, descriptor));
	}

	Optional<String> getUri(Object thing, Map<String, Object> properties) {
		String nsUri = (String) properties.get(EmfApi.NS_URI);
		Optional<String> result;
		if (nsUri == null || nsUri.isEmpty()) {
			String message = String.format("%s did not specify an NS URI via its properties, ignoring!", thing);
			logger.log(LogService.LOG_WARNING, message);
			result = Optional.empty();
		} else {
			result = Optional.of(nsUri);
		}
		return result;

	}

	@Override
	public EPackage getEPackage(String nsURI) {
		final Object thing = get(nsURI);
		EPackage result = null;
		if (thing instanceof EPackage) {
			result = (EPackage) thing;
		} else if (thing instanceof EPackage.Descriptor) {
			result = ((EPackage.Descriptor) thing).getEPackage();

		}
		return result;

	}

	@Override
	public EFactory getEFactory(String nsURI) {
		final Object thing = get(nsURI);
		if (thing instanceof EPackage) {
			return ((EPackage) thing).getEFactoryInstance();
		} else if (thing instanceof EPackage.Descriptor) {
			return ((EPackage.Descriptor) thing).getEFactory();
		}
		return null;
	}

	@Override
	public Object get(Object key) {
		return delegate.get(key);
	}

	@Override
	public Object put(String key, Object value) {
		if (value instanceof EPackage || value instanceof EPackage.Descriptor) {
			return delegate.put(key, value);
		}
		final String message = String.format("Object '%s' is not of type EPackage or EPackage.Descriptor via NS URI %s",
				value, key);
		final RuntimeException e = new IllegalArgumentException(message);
		logger.log(LogService.LOG_WARNING, message, e);
		throw e;
	}

	@Override
	public int size() {
		return delegate.size();
	}

	@Override
	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return delegate.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return delegate.containsKey(value);
	}

	@Override
	public Object remove(Object key) {
		return delegate.remove(key);
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> m) {
		m.entrySet().stream().forEach(es -> put(es.getKey(), es.getValue()));
	}

	@Override
	public void clear() {
		delegate.clear();
	}

	@Override
	public Set<String> keySet() {
		return delegate.keySet();
	}

	@Override
	public Collection<Object> values() {
		return delegate.values();
	}

	@Override
	public Set<Entry<String, Object>> entrySet() {
		return delegate.entrySet();
	}

}
