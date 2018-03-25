package io.blesmol.emf.cdo.provider;

import java.util.Map;

import org.eclipse.net4j.util.container.IManagedContainer;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import io.blesmol.emf.cdo.api.CdoApi;
import io.blesmol.emf.cdo.api.CdoViewProvider;
import io.blesmol.emf.cdo.impl.CdoViewProviderImpl;

@Component(configurationPid = CdoApi.CdoViewProvider.PID, configurationPolicy = ConfigurationPolicy.REQUIRE, service = CdoViewProvider.class, immediate = true)
public class CdoViewProviderProvider extends CdoViewProviderImpl {

	private String servicePid;

	@Reference(name = CdoApi.CdoViewProvider.Reference.CONTAINER)
	void setContainer(IManagedContainer container) {
		this.container = container;
	}

	void unsetContainer(IManagedContainer container) {
		this.container = null;
	}

	@Activate
	void activate(CdoApi.CdoViewProvider config, Map<String, Object> properties) {
		this.servicePid = (String) properties.getOrDefault(Constants.SERVICE_PID, super.toString());
		this.setRegex(config.blesmol_cdoviewprovider_regex());
		this.setPriority(config.blesmol_cdoviewprovider_priority());
		super.activate();
	}

	@Override
	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}

	@Override
	public String toString() {
		return servicePid;
	}
}
