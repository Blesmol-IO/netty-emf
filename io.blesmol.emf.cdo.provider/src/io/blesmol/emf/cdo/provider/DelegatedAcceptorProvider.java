package io.blesmol.emf.cdo.provider;

import org.eclipse.net4j.acceptor.IAcceptor;
import org.eclipse.net4j.util.container.IManagedContainer;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import io.blesmol.emf.cdo.api.CdoApi;
import io.blesmol.emf.cdo.impl.DelegatedAcceptor;

@Component(configurationPid=CdoApi.IAcceptor.PID, configurationPolicy=ConfigurationPolicy.REQUIRE, service=IAcceptor.class)
public class DelegatedAcceptorProvider extends DelegatedAcceptor {

	@Reference(name = CdoApi.IAcceptor.Reference.MANAGED_CONTAINER)
	void setContainer(IManagedContainer container) {
		this.container = container;
	}

	void unsetContainer(IManagedContainer container) {
		this.container = null;
	}
	
	@Activate
	void activate(CdoApi.IAcceptor config) {
		super.activate(config.type(), config.description());
	}
	
	@Deactivate
	void deactivate(CdoApi.IAcceptor config) {
		super.deactivate();
	}
}
