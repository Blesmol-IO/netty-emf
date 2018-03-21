package io.blesmol.emf.cdo.provider;

import java.util.Map;

import org.eclipse.net4j.acceptor.IAcceptor;
import org.eclipse.net4j.util.container.IManagedContainer;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import io.blesmol.emf.cdo.api.CdoApi;
import io.blesmol.emf.cdo.impl.DelegatedAcceptor;

@Component(configurationPid = CdoApi.IAcceptor.PID, configurationPolicy = ConfigurationPolicy.REQUIRE, service = IAcceptor.class)
public class DelegatedAcceptorProvider extends DelegatedAcceptor {

	private String servicePid;

	@Reference(name = CdoApi.IAcceptor.Reference.MANAGED_CONTAINER)
	void setContainer(IManagedContainer container) {
		this.container = container;
	}

	void unsetContainer(IManagedContainer container) {
		this.container = null;
	}

	@Activate
	void activate(CdoApi.IAcceptor config, Map<String, Object> properties) {
		this.servicePid = (String) properties.getOrDefault(Constants.SERVICE_PID, super.toString());

		super.activate(config.emf_cdo_acceptor_type(), config.emf_cdo_acceptor_description());
	}

	@Deactivate
	void deactivate(CdoApi.IAcceptor config) {
		super.deactivate();
	}

	@Override
	public String toString() {
		return servicePid;
	}
}
