package com.paremus.datanucleus.extender;

import java.net.URI;
import java.util.Properties;

import javax.jdo.PersistenceManagerFactory;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

import com.paremus.datanucleus.api.DatabaseConnectionConfigurer;
import com.paremus.datanucleus.api.PersistenceManagerFactoryBuilder;

public class PMFBTracker extends ServiceTracker {

	private DatabaseConnectionConfigurer configurer;
	private URI publishedAppUri;

	public PMFBTracker(BundleContext context, DatabaseConnectionConfigurer configurer, URI publishedAppUri) {
		super(context, PersistenceManagerFactoryBuilder.class.getName(), null);
		this.configurer = configurer;
		this.publishedAppUri = publishedAppUri;
	}

	@Override
	public Object addingService(ServiceReference reference) {
		String modelName = (String) reference.getProperty(PersistenceManagerFactoryBuilder.PROP_MODEL_NAME);
		PersistenceManagerFactoryBuilder pmfb = (PersistenceManagerFactoryBuilder) context.getService(reference);
		System.out.println("PMFB added");
		
		try {
			PersistenceManagerFactory pmf = configurer.configure(pmfb, publishedAppUri);
			Properties pmfSvcProps = new Properties();
			pmfSvcProps.put(PersistenceManagerFactoryBuilder.PROP_MODEL_NAME, modelName);
			
			return context.registerService(PersistenceManagerFactory.class.getName(), pmf, pmfSvcProps);
		} catch (Exception e) {
			e.printStackTrace();
			context.ungetService(reference);
			return null;
		}
	}
	
	@Override
	public void removedService(ServiceReference reference, Object service) {
		ServiceRegistration registration = (ServiceRegistration) service;
		registration.unregister();
		context.ungetService(reference);
	}

}
