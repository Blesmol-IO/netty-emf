package io.blesmol.emf.cdo.db.h2.provider;

import org.eclipse.net4j.db.IDBAdapter;
import org.eclipse.net4j.db.IDBConnectionProvider;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;

import io.blesmol.emf.cdo.api.CdoApi;
import io.blesmol.emf.cdo.db.h2.impl.H2FileDbConnectionProvider;

@Component(configurationPid=CdoApi.IDBConnectionProvider.PID, configurationPolicy=ConfigurationPolicy.REQUIRE, service=IDBConnectionProvider.class, immediate=true)
public class H2FileDbConnectionProviderProvider extends H2FileDbConnectionProvider {
	
	@Reference(name = CdoApi.IDBConnectionProvider.Reference.DB_ADAPTER)
	void setDBAdapter(IDBAdapter dbAdapter) {
		this.dbAdapter = dbAdapter;
	}
	void unsetDBAdapter(IDBAdapter dbAdapter) {
		this.dbAdapter = null;
	}
	
	@Activate
	void activate(BundleContext context, CdoApi.IDBConnectionProvider config) {
		super.activate(config.emf_cdo_connectionprovider_url());
	}
}
