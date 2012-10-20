package com.paremus.example.datanucleus.service;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.jdo.PersistenceManagerFactory;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

import com.paremus.datanucleus.api.PersistenceManagerFactoryBuilder;

public class LazyPMF implements ServiceFactory {
    
    private final PersistenceManagerFactoryBuilder builder;
    private final AtomicInteger count = new AtomicInteger(0);
    private final Map<String,Object> properties;
    
    private PersistenceManagerFactory pmf = null;

    public LazyPMF(PersistenceManagerFactoryBuilder builder, Map<String, Object> properties) {
        this.builder = builder;
        this.properties = properties;
    }

    public Object getService(Bundle bundle, ServiceRegistration registration) {
        if (count.getAndIncrement() == 0) {
            try {
                pmf = builder.createPersistenceManagerFactory(properties);
            } catch (Exception e) {
                e.printStackTrace();
                pmf = null;
            }
        }
        return pmf;
    }

    public void ungetService(Bundle bundle, ServiceRegistration registration, Object service) {
        if (count.decrementAndGet() == 0) {
            if (pmf != null) pmf.close();
        }
    }

}
