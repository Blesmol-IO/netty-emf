package io.blesmol.emf.cdo.provider;

import java.util.Map;

import org.eclipse.emf.cdo.eresource.impl.CDOResourceFactoryImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

import io.blesmol.emf.api.EmfApi;

// TODO: auto-generate
@Component(configurationPid = EmfApi.Resource_Factory.PID, configurationPolicy = ConfigurationPolicy.REQUIRE, service = Resource.Factory.class, immediate = true, property = {
		EmfApi.SCHEME + "=cdo.net4j.jvm", EmfApi.SCHEME + "=cdo.net4j.tcp", EmfApi.SCHEME + "=cdo.net4j.ssl" })
public class CdoResourceFactoryProvider extends CDOResourceFactoryImpl {

	protected String toString;

	@Activate
	void activate(Map<String, Object> properties) {
		toString = (String) properties.getOrDefault(Constants.SERVICE_PID, getClass().getName());
	}

	@Override
	public String toString() {
		return toString;
	}
}
