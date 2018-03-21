package io.blesmol.emf.cdo.provider;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.eclipse.net4j.acceptor.IAcceptor;
import org.eclipse.net4j.db.IDBAdapter;
import org.eclipse.net4j.util.container.IManagedContainer;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import io.blesmol.emf.cdo.CdoServerImpl;
import io.blesmol.emf.cdo.api.CdoApi;
import io.blesmol.emf.cdo.api.CdoServer;

@Component(configurationPid = CdoApi.CdoServer.PID, configurationPolicy = ConfigurationPolicy.REQUIRE, service = CdoServer.class)
public class CdoServerProvider extends CdoServerImpl {

	@Reference(name = CdoApi.CdoServer.Reference.DATA_SOURCE)
	void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	void unsetDataSource(DataSource dataSource) {
		this.dataSource = null;
	}

	@Reference(name = CdoApi.CdoServer.Reference.DB_ADAPTER)
	void setDbAdapter(IDBAdapter dbAdapter) {
		this.dbAdapter = dbAdapter;
	}

	void unsetDbAdapter(IDBAdapter dbAdapter) {
		this.dbAdapter = null;
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
	void activate(CdoApi.CdoServer config, Map<String, Object> properties) {
		// TODO: Run on thread
		final Map<String, String> repoProps = repoProperties(properties);
		activate(config.repoName(), config.auditing(), config.branching(), config.withRanges(), repoProps);
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
}
