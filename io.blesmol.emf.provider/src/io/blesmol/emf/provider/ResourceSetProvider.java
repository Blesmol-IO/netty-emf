package io.blesmol.emf.provider;

import java.util.Map;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.emf.ecore.resource.Resource.Factory;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import io.blesmol.emf.api.EmfApi;

@Component(configurationPid = EmfApi.ResourceSet.PID, configurationPolicy=ConfigurationPolicy.REQUIRE, service = ResourceSet.class)
public class ResourceSetProvider extends ResourceSetImpl {

	private String servicePid;

	private volatile URIConverter converter;
	private volatile EPackage.Registry ePackageRegistry;
	private volatile Factory.Registry factoryRegistry;

	@Reference(name = EmfApi.ResourceSet.Reference.URI_CONVERTER, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	@Override
	public void setURIConverter(URIConverter uriConverter) {
		this.converter = uriConverter;
	}

	void unsetURIConverter(URIConverter converter) {
		this.converter = null;
	}

	@Override
	public URIConverter getURIConverter() {
		return this.converter;
	}

	@Reference(name = EmfApi.ResourceSet.Reference.EPACKAGE_REGISTRY, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	@Override
	public void setPackageRegistry(EPackage.Registry packageRegistry) {
		this.ePackageRegistry = packageRegistry;
	}

	void unsetPackageRegistry(EPackage.Registry packageRegistry) {
		this.ePackageRegistry = null;
	}

	@Override
	public Registry getPackageRegistry() {
		return ePackageRegistry;
	}

	@Reference(name = EmfApi.ResourceSet.Reference.RESOURCE_FACTORY_REGISTRY, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	@Override
	public void setResourceFactoryRegistry(Factory.Registry factoryRegistry) {
		this.factoryRegistry = factoryRegistry;
	}

	void unsetResourceFactoryRegistry(Factory.Registry factoryRegistry) {
		this.factoryRegistry = null;
	}

	@Override
	public Factory.Registry getResourceFactoryRegistry() {
		return factoryRegistry;
	}

	@Activate
	void activate(Map<String, Object> properties) {
		this.servicePid = (String) properties.get(Constants.SERVICE_PID);
	}

	@Override
	public String toString() {
		return servicePid;
	}

}
