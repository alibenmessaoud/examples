package com.paremus.datanucleus.extender.test;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.PersistenceManagerFactory;

import com.paremus.datanucleus.api.DatabaseConnectionConfigurer;
import com.paremus.datanucleus.api.PersistenceManagerFactoryBuilder;

public class MockDatabaseConfigurer implements DatabaseConnectionConfigurer {

	@Override
	public PersistenceManagerFactory configure(PersistenceManagerFactoryBuilder pmfBuilder, URI uri) throws Exception {
		String jdbcUri = "jdbc:dummy:" + uri.getHost() + ":" + uri.getPort() + "/DummyDB";
		
		Map<String, Object> props = new HashMap<String, Object>();
		props.put("javax.jdo.option.ConnectionURL", jdbcUri);
		
		return pmfBuilder.createPersistenceManagerFactory(props);
	}

}
