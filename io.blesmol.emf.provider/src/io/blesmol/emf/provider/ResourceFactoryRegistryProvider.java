package io.blesmol.emf.provider;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.emf.ecore.resource.Resource.Factory;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.impl.ResourceFactoryRegistryImpl;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import io.blesmol.emf.api.EmfApi;

@Component(configurationPid = EmfApi.Resource_Factory_Registry.PID, configurationPolicy=ConfigurationPolicy.REQUIRE, service = Factory.Registry.class, immediate=true)
public class ResourceFactoryRegistryProvider extends ResourceFactoryRegistryImpl {

	private volatile URIConverter converter;

	public ResourceFactoryRegistryProvider() {
		this.protocolToFactoryMap = new ConcurrentHashMap<>();
		this.contentTypeIdentifierToFactoryMap = new ConcurrentHashMap<>();
		this.extensionToFactoryMap = new ConcurrentHashMap<>();
	}

	@Reference(name = EmfApi.Resource_Factory_Registry.Reference.URI_CONVERTER, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	void setURIConverter(URIConverter uriConverter) {
		this.converter = uriConverter;
	}

	void unsetURIConverter(URIConverter converter) {
		this.converter = null;
	}

	@Reference(name = EmfApi.Resource_Factory_Registry.Reference.RESOURCE_FACTORIES, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MULTIPLE)
	void setFactory(Factory factory, Map<String, Object> properties) {
		get(properties, EmfApi.SCHEME).ifPresent(s -> protocolToFactoryMap.put(s, factory));
		get(properties, EmfApi.EXTENSION).ifPresent(s -> extensionToFactoryMap.put(s, factory));
		get(properties, EmfApi.CONTENT_TYPE).ifPresent(s -> contentTypeIdentifierToFactoryMap.put(s, factory));
	}

	void unsetFactory(Factory factory, Map<String, Object> properties) {
		get(properties, EmfApi.SCHEME).ifPresent(s -> protocolToFactoryMap.remove(s, factory));
		get(properties, EmfApi.EXTENSION).ifPresent(s -> extensionToFactoryMap.remove(s, factory));
		get(properties, EmfApi.CONTENT_TYPE).ifPresent(s -> contentTypeIdentifierToFactoryMap.remove(s, factory));
	}

	@Reference(name = EmfApi.Resource_Factory_Registry.Reference.RESOURCE_FACTORY_DESCRIPTORS, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MULTIPLE)
	void setDescriptor(Factory.Descriptor descriptor, Map<String, Object> properties) {
		get(properties, EmfApi.SCHEME).ifPresent(s -> protocolToFactoryMap.put(s, descriptor));
		get(properties, EmfApi.EXTENSION).ifPresent(s -> extensionToFactoryMap.put(s, descriptor));
		get(properties, EmfApi.CONTENT_TYPE).ifPresent(s -> contentTypeIdentifierToFactoryMap.put(s, descriptor));
	}

	void unsetDescriptor(Factory.Descriptor descriptor, Map<String, Object> properties) {
		get(properties, EmfApi.SCHEME).ifPresent(s -> protocolToFactoryMap.remove(s, descriptor));
		get(properties, EmfApi.EXTENSION).ifPresent(s -> extensionToFactoryMap.remove(s, descriptor));
		get(properties, EmfApi.CONTENT_TYPE).ifPresent(s -> contentTypeIdentifierToFactoryMap.remove(s, descriptor));
	}

	Optional<String> get(Map<String, Object> properties, String key) {
		return Optional.ofNullable((String) properties.get(key));
	}

	@Override
	protected URIConverter getURIConverter() {
		return converter;
	}

}
