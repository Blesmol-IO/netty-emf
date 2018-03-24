package io.blesmol.emf.provider;

import java.util.Arrays;
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

@Component(configurationPid = EmfApi.Resource_Factory_Registry.PID, configurationPolicy = ConfigurationPolicy.REQUIRE, service = Factory.Registry.class, immediate = true)
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

	void putOrRemoveIntoFactoryMap(boolean put, Map<String, Object> properties, String apiName,
			Map<String, Object> factoryMap, Object factoryDescriptor) {
		Optional.ofNullable((String[]) properties.get(apiName))
				.ifPresent(array -> Arrays.stream(array).forEach((key) -> {
					if (put)
						factoryMap.put(key, factoryDescriptor);
					else
						factoryMap.remove(key, factoryDescriptor);
				}));
	}

	@Reference(name = EmfApi.Resource_Factory_Registry.Reference.RESOURCE_FACTORIES, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MULTIPLE)
	void setFactory(Factory factory, Map<String, Object> properties) {
		putOrRemoveIntoFactoryMap(true, properties, EmfApi.SCHEME, protocolToFactoryMap, factory);
		putOrRemoveIntoFactoryMap(true, properties, EmfApi.EXTENSION, extensionToFactoryMap, factory);
		putOrRemoveIntoFactoryMap(true, properties, EmfApi.CONTENT_TYPE, contentTypeIdentifierToFactoryMap, factory);
	}

	void unsetFactory(Factory factory, Map<String, Object> properties) {
		putOrRemoveIntoFactoryMap(false, properties, EmfApi.SCHEME, protocolToFactoryMap, factory);
		putOrRemoveIntoFactoryMap(false, properties, EmfApi.EXTENSION, extensionToFactoryMap, factory);
		putOrRemoveIntoFactoryMap(false, properties, EmfApi.CONTENT_TYPE, contentTypeIdentifierToFactoryMap, factory);
	}

	// TODO: consider removing and targeting a reference with appropriate object
	// classes Factory & Factory.Descriptor
	@Reference(name = EmfApi.Resource_Factory_Registry.Reference.RESOURCE_FACTORY_DESCRIPTORS, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MULTIPLE)
	void setDescriptor(Factory.Descriptor descriptor, Map<String, Object> properties) {
		putOrRemoveIntoFactoryMap(true, properties, EmfApi.SCHEME, protocolToFactoryMap, descriptor);
		putOrRemoveIntoFactoryMap(true, properties, EmfApi.EXTENSION, extensionToFactoryMap, descriptor);
		putOrRemoveIntoFactoryMap(true, properties, EmfApi.CONTENT_TYPE, contentTypeIdentifierToFactoryMap, descriptor);

	}

	void unsetDescriptor(Factory.Descriptor descriptor, Map<String, Object> properties) {
		putOrRemoveIntoFactoryMap(true, properties, EmfApi.SCHEME, protocolToFactoryMap, descriptor);
		putOrRemoveIntoFactoryMap(true, properties, EmfApi.EXTENSION, extensionToFactoryMap, descriptor);
		putOrRemoveIntoFactoryMap(true, properties, EmfApi.CONTENT_TYPE, contentTypeIdentifierToFactoryMap, descriptor);
	}

	@Override
	protected URIConverter getURIConverter() {
		return converter;
	}

}
