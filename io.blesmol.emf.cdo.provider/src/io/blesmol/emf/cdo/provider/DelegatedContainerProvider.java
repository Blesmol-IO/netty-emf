package io.blesmol.emf.cdo.provider;

import org.eclipse.net4j.util.container.IManagedContainer;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;

import io.blesmol.emf.cdo.api.CdoApi;
import io.blesmol.emf.cdo.impl.DelegatedContainer;

@Component(configurationPid=CdoApi.IManagedContainer.PID, configurationPolicy=ConfigurationPolicy.REQUIRE, service = IManagedContainer.class)
public class DelegatedContainerProvider extends DelegatedContainer {

	@Activate
	void activate(CdoApi.IManagedContainer config) {
		super.activate(config.type());
	}
	
	@Deactivate
	void deactivate(CdoApi.IManagedContainer config) {
		super.deactivate();
	}
}
