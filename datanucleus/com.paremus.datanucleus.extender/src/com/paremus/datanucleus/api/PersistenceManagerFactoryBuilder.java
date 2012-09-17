package com.paremus.datanucleus.api;

import java.util.Map;

import javax.jdo.PersistenceManagerFactory;

public interface PersistenceManagerFactoryBuilder {

	public PersistenceManagerFactory createPersistenceManagerFactory(Map<String, Object> properties) throws Exception;

}
