package io.blesmol.emf.cdo.db.h2.provider;

import org.eclipse.net4j.db.IDBAdapter;
import org.eclipse.net4j.db.h2.H2Adapter;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

import io.blesmol.emf.cdo.api.CdoApi;

@Component(configurationPid = CdoApi.H2Adapter.PID, configurationPolicy = ConfigurationPolicy.REQUIRE, service = IDBAdapter.class)
public class H2AdapterProvider extends H2Adapter {

}
