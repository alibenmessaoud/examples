package com.paremus.datanucleus.extender;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import com.paremus.datanucleus.api.DatabaseConnectionConfigurer;

public class ConnectionConfigurerTracker extends ServiceTracker {

	public ConnectionConfigurerTracker(BundleContext context) {
		super(context, DatabaseConnectionConfigurer.class.getName(), null);
	}
	
	@Override
	public Object addingService(ServiceReference reference) {
		String matchProp = (String) reference.getProperty(DatabaseConnectionConfigurer.PROP_URI_MATCH);
		if (matchProp == null)
			return null;
		System.out.println("Adding configurer with uriMatch " + matchProp);
		
		DatabaseConnectionConfigurer configurer = (DatabaseConnectionConfigurer) context.getService(reference);
		EndpointTracker appTracker = new EndpointTracker(context, configurer, matchProp);
		appTracker.open();
		
		return appTracker;
	}
	
	@Override
	public void removedService(ServiceReference reference, Object service) {
		ServiceTracker tracker = (ServiceTracker) service;
		tracker.close();
		
		context.ungetService(reference);
	}
	
}
