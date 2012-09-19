package com.paremus.datanucleus.api;

import java.net.URI;

import javax.jdo.PersistenceManagerFactory;

public interface DatabaseConnectionConfigurer {
	
	static final String PROP_URI_MATCH = "uriMatch";
	
	PersistenceManagerFactory configure(PersistenceManagerFactoryBuilder pmfBuilder, URI dataSourceUri) throws Exception;

}
