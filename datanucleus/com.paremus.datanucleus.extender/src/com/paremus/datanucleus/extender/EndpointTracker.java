package com.paremus.datanucleus.extender;

import java.net.URI;

import org.bndtools.service.endpoint.Endpoint;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import com.paremus.datanucleus.api.DatabaseConnectionConfigurer;

public class EndpointTracker extends ServiceTracker {

	private DatabaseConnectionConfigurer configurer;

	public EndpointTracker(BundleContext context, DatabaseConnectionConfigurer configurer, String uriMatch) {
		super(context, buildFilter(uriMatch), null);
		this.configurer = configurer;
	}
	
	private static Filter buildFilter(String uriMatch) {
		try {
			return FrameworkUtil.createFilter(String.format("(&(objectClass=%s)(uri=%s))", Endpoint.class.getName(), uriMatch));
		} catch (InvalidSyntaxException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public Object addingService(ServiceReference reference) {
		String uri = (String) reference.getProperty("uri");
		System.out.println("Added published app with uri " + uri);
		
		try {
			PMFBTracker pmfbTracker = new PMFBTracker(context, configurer, new URI(uri));
			pmfbTracker.open();
			
			return pmfbTracker;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public void removedService(ServiceReference reference, Object service) {
		ServiceTracker tracker = (ServiceTracker) service;
		tracker.close();
	}
}
