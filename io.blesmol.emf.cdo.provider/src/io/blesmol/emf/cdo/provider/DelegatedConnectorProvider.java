package io.blesmol.emf.cdo.provider;

import org.eclipse.net4j.connector.IConnector;
import org.eclipse.net4j.util.container.IManagedContainer;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import io.blesmol.emf.cdo.api.CdoApi;
import io.blesmol.emf.cdo.impl.DelegatedConnector;

@Component(configurationPid = CdoApi.IConnector.PID, configurationPolicy = ConfigurationPolicy.REQUIRE, service = IConnector.class)
public class DelegatedConnectorProvider extends DelegatedConnector {

	@Reference(name = CdoApi.IConnector.Reference.MANAGED_CONTAINER)
	void setContainer(IManagedContainer container) {
		this.container = container;
	}

	void unsetContainer(IManagedContainer container) {
		this.container = null;
	}

	@Activate
	protected void activate(CdoApi.IConnector config) {
		super.activate(config.productGroup(), config.type(), config.description());
	}

	@Deactivate
	@Override
	protected void deactivate() {
		super.deactivate();
	}
}
