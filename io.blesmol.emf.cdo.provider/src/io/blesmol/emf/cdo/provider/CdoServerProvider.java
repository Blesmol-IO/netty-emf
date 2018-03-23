package io.blesmol.emf.cdo.provider;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.net4j.acceptor.IAcceptor;
import org.eclipse.net4j.db.IDBAdapter;
import org.eclipse.net4j.db.IDBConnectionProvider;
import org.eclipse.net4j.util.container.IManagedContainer;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import io.blesmol.emf.cdo.api.CdoApi;
import io.blesmol.emf.cdo.api.CdoServer;
import io.blesmol.emf.cdo.impl.CdoServerImpl;

@Component(configurationPid = CdoApi.CdoServer.PID, configurationPolicy = ConfigurationPolicy.REQUIRE, service = CdoServer.class, immediate=true)
public class CdoServerProvider extends CdoServerImpl {

	private String servicePid;

	@Reference(name = CdoApi.CdoServer.Reference.DB_ADAPTER)
	void setDbAdapter(IDBAdapter dbAdapter) {
		this.dbAdapter = dbAdapter;
	}

	void unsetDbAdapter(IDBAdapter dbAdapter) {
		this.dbAdapter = null;
	}
	
	@Reference(name = CdoApi.CdoServer.Reference.DB_CONNECTION_PROVIDER)
	void setDbConnectionProvider(IDBConnectionProvider dbConnectionProvider) {
		this.connectionProvider = dbConnectionProvider;
	}
	
	void unsetDbConnectionProvider(IDBConnectionProvider dbConnectionProvider) {
		this.connectionProvider = null;
	}

	@Reference(name = CdoApi.CdoServer.Reference.MANAGED_CONTAINER)
	void setContainer(IManagedContainer container) {
		this.container = container;
	}

	void unsetContainer(IManagedContainer container) {
		this.container = null;
	}

	@Reference(name = CdoApi.CdoServer.Reference.ACCEPTOR)
	void setAcceptor(IAcceptor acceptor) {
		this.acceptor = acceptor;
	}

	void unsetAcceptor(IAcceptor acceptor) {
		this.acceptor = null;
	}

	@Activate
	void activate(CdoApi.CdoServer config, Map<String, Object> properties) throws Exception {
		// TODO: Run on thread
		this.servicePid = (String) properties.getOrDefault(Constants.SERVICE_PID, super.toString());
		final Map<String, String> repoProps = repoProperties(properties);
		activate(config.blesmol_cdoserver_reponame(), config.blesmol_cdoserver_auditing(),
				config.blesmol_cdoserver_branching(), config.blesmol_cdoserver_withranges(), repoProps);
	}

	@Override
	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}

	/**
	 * Copies all string values to string map
	 */
	Map<String, String> repoProperties(Map<String, Object> properties) {
		final Map<String, String> results = new HashMap<>();
		properties.entrySet().stream().filter(es -> (es.getValue() instanceof String))
				.forEach(es -> results.put(es.getKey(), (String) es.getValue()));
		return results;
	}

	@Override
	public String toString() {
		return servicePid;
	}

}
