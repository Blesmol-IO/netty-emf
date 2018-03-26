package io.blesmol.emf.cdo.db.h2.impl;

import java.io.File;
import java.net.URI;
import java.sql.Connection;

import org.eclipse.net4j.db.DBException;
import org.eclipse.net4j.db.IDBAdapter;
import org.eclipse.net4j.db.IDBConnectionProvider;
import org.eclipse.net4j.db.h2.H2Adapter;
import org.h2.jdbcx.JdbcDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.blesmol.emf.cdo.db.h2.api.H2Api;

public class H2FileDbConnectionProvider implements IDBConnectionProvider {

	protected static final Logger logger = LoggerFactory.getLogger(IDBConnectionProvider.class);

	protected IDBAdapter dbAdapter;

	protected IDBConnectionProvider delegate;

	/**
	 * Repository name is derived from file URL last path component and assumes
	 * MVStore data formats
	 * 
	 * @throws Exception
	 */
	protected void activate(String fileUrl) throws Exception {
		final String dbUri = "jdbc:h2:" + fileUrl;
		final String repoName = new File(fileUrl).getName();
		final File file = getFile(fileUrl);
		final JdbcDataSource dataSource = new JdbcDataSource();
		if (file.length() == 0) {
			logger.debug("Creating new schema for dbUri {} and repository at {}", dbUri, file.getAbsolutePath());
			dataSource.setUrl(dbUri);
			H2Adapter.createSchema(dataSource, repoName, true);
		}
		dataSource.setURL(dbUri + ";SCHEMA=" + repoName);
		delegate = dbAdapter.createConnectionProvider(dataSource);
	}

	protected File getFile(String fileUrl) throws Exception {
		URI uri = new URI(fileUrl + H2Api.MV_STORE_SUFFIX);
		return new File(uri);
	}

	@Override
	public Connection getConnection() throws DBException {
		return delegate.getConnection();
	}

}
