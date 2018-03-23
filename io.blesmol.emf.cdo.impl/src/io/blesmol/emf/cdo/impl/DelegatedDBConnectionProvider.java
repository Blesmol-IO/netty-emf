package io.blesmol.emf.cdo.impl;

import java.sql.Connection;

import org.eclipse.net4j.db.DBException;
import org.eclipse.net4j.db.IDBConnectionProvider;

public class DelegatedDBConnectionProvider implements IDBConnectionProvider {

	private IDBConnectionProvider delegate;
	
	protected void activate() {
		
	}
	
	
	@Override
	public Connection getConnection() throws DBException {
		return null;
	}

}
