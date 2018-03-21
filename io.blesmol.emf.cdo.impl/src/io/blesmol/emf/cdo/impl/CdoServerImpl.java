package io.blesmol.emf.cdo.impl;

import java.util.Map;

import javax.sql.DataSource;

import org.eclipse.emf.cdo.server.CDOServerUtil;
import org.eclipse.emf.cdo.server.IRepository;
import org.eclipse.emf.cdo.server.IStore;
import org.eclipse.emf.cdo.server.db.CDODBUtil;
import org.eclipse.emf.cdo.server.db.mapping.IMappingStrategy;
import org.eclipse.net4j.acceptor.IAcceptor;
import org.eclipse.net4j.db.IDBAdapter;
import org.eclipse.net4j.db.IDBConnectionProvider;
import org.eclipse.net4j.util.container.IManagedContainer;
import org.eclipse.net4j.util.lifecycle.LifecycleUtil;

import io.blesmol.emf.cdo.api.CdoServer;

public class CdoServerImpl implements CdoServer {

	// Passed in
	protected DataSource dataSource;
	protected IDBAdapter dbAdapter;
	protected IManagedContainer container;
	protected IAcceptor acceptor;

	// Created
	protected IDBConnectionProvider connectionProvider;
	protected IMappingStrategy mappingStrategy;
	protected IStore store;
	protected IRepository repository;

	protected void activate(String repoName, boolean auditing, boolean branching, boolean withRanges,
			Map<String, String> repoProps) {
		connectionProvider = dbAdapter.createConnectionProvider(dataSource);
		mappingStrategy = CDODBUtil.createHorizontalMappingStrategy(auditing, branching, withRanges);
		store = CDODBUtil.createStore(mappingStrategy, dbAdapter, connectionProvider);
		// container = container();
		repository = CDOServerUtil.createRepository(repoName, store, repoProps);
		CDOServerUtil.addRepository(container, repository);
	}

	protected void deactivate() {
		acceptor.close();
		repository.deactivate();
		container.deactivate();
		LifecycleUtil.deactivate(store);
	}

}
