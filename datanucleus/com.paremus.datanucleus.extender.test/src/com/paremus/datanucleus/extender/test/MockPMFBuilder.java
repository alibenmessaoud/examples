package com.paremus.datanucleus.extender.test;

import static org.mockito.Mockito.mock;

import java.util.Map;

import javax.jdo.PersistenceManagerFactory;

import junit.framework.AssertionFailedError;

import com.paremus.datanucleus.api.PersistenceManagerFactoryBuilder;

public class MockPMFBuilder implements PersistenceManagerFactoryBuilder {
	
	private Map<String, Object> props = null;
	
	@Override
	public PersistenceManagerFactory createPersistenceManagerFactory(Map<String, Object> properties) throws Exception {
		if (props != null)
			throw new AssertionFailedError("Should not call createPersistenceManagerFactory twice");
		this.props = properties;
		return mock(PersistenceManagerFactory.class);
	}
	
	Map<String, Object> getProps() {
		return props;
	}

}
