package io.blesmol.emf.cdo.provider;

import java.util.Map;

import org.eclipse.net4j.connector.IConnector;
import org.eclipse.net4j.util.container.IManagedContainer;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import io.blesmol.emf.cdo.api.CdoApi;
import io.blesmol.emf.cdo.impl.DelegatedConnector;

@Component(configurationPid = CdoApi.IConnector.PID, configurationPolicy = ConfigurationPolicy.REQUIRE, service = IConnector.class, immediate=true)
public class DelegatedConnectorProvider extends DelegatedConnector {

	private String servicePid;

	@Reference(name = CdoApi.IConnector.Reference.MANAGED_CONTAINER)
	void setContainer(IManagedContainer container) {
		this.container = container;
	}

	void unsetContainer(IManagedContainer container) {
		this.container = null;
	}

	@Activate
	protected void activate(CdoApi.IConnector config, Map<String, Object> properties) {
		this.servicePid = (String) properties.getOrDefault(Constants.SERVICE_PID, super.toString());
		super.activate(config.emf_cdo_connector_productgroup(), config.emf_cdo_connector_type(),
				config.emf_cdo_connector_description());
	}

	@Deactivate
	@Override
	protected void deactivate() {
		super.deactivate();
	}

	@Override
	public String toString() {
		return servicePid;
	}
}
