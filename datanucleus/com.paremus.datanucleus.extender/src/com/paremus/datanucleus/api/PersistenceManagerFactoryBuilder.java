package com.paremus.datanucleus.api;

import java.util.Map;

import javax.jdo.PersistenceManagerFactory;

public interface PersistenceManagerFactoryBuilder {
	
	static final String PROP_MODEL_NAME = "com.paremus.datanucleus.model";

	PersistenceManagerFactory createPersistenceManagerFactory(Map<String, Object> properties) throws Exception;

}
