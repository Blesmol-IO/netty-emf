package io.blesmol.emf.cdo.provider;

import java.util.Map;

import org.eclipse.net4j.util.container.IManagedContainer;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;

import io.blesmol.emf.cdo.api.CdoApi;
import io.blesmol.emf.cdo.impl.DelegatedContainer;

@Component(configurationPid = CdoApi.IManagedContainer.PID, configurationPolicy = ConfigurationPolicy.REQUIRE, service = IManagedContainer.class)
public class DelegatedContainerProvider extends DelegatedContainer {

	private String servicePid;

	@Activate
	void activate(CdoApi.IManagedContainer config, Map<String, Object> properties) {
		this.servicePid = (String) properties.getOrDefault(Constants.SERVICE_PID, super.toString());
		super.activate(config.emf_cdo_managedcontainer_type());
	}

	// Use a config to avoid overriding super
	@Deactivate
	void deactivate(CdoApi.IManagedContainer config) {
		super.deactivate();
	}

	@Override
	public String toString() {
		return servicePid;
	}

}
