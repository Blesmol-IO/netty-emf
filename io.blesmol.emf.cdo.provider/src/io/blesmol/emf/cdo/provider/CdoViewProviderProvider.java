package io.blesmol.emf.cdo.provider;

import org.eclipse.net4j.connector.IConnector;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import io.blesmol.emf.cdo.CdoViewProviderImpl;
import io.blesmol.emf.cdo.api.CdoApi;
import io.blesmol.emf.cdo.api.CdoViewProvider;

@Component(configurationPid=CdoApi.CdoViewProvider.PID, configurationPolicy=ConfigurationPolicy.REQUIRE, service=CdoViewProvider.class)
public class CdoViewProviderProvider extends CdoViewProviderImpl {

	@Reference(name=CdoApi.CdoViewProvider.Reference.CONNECTOR)
	void setConnector(IConnector connector) {
		this.connector = connector;
	}
	void unsetConnecto(IConnector connector) {
		this.connector = null;
	}
	
	@Activate
	void activate(CdoApi.CdoViewProvider config) {
		this.setRegex(config.regex());
		this.setPriority(config.priority());
		super.activate();
	}
	
	@Override
	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}
}
