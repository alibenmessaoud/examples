package com.paremus.packager.whiteboard;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

import com.paremus.packager.api.PackagerException;
import com.paremus.packager.api.Scope;
import com.paremus.packager.api.discovery.ApplicationDiscoveryEvent;
import com.paremus.packager.api.discovery.ApplicationDiscoveryListener;
import com.paremus.packager.api.discovery.ApplicationDiscoveryService;
import com.paremus.packager.api.discovery.ApplicationDiscoveryServiceFactory;
import com.paremus.packager.api.discovery.ApplicationServiceReference;
import com.paremus.packager.whiteboard.api.PublishedApplication;

public class AppDiscFactoryTracker extends ServiceTracker implements ApplicationDiscoveryListener {

	private final Map<String, ServiceRegistration> registrations = new HashMap<String, ServiceRegistration>();
	private final Scope scope;

	public AppDiscFactoryTracker(BundleContext context, String scopeName) {
		super(context, ApplicationDiscoveryServiceFactory.class.getName(), null);
		this.scope = new Scope(scopeName);
	}
	
	@Override
	public Object addingService(ServiceReference reference) {
		ApplicationDiscoveryServiceFactory discServiceFactory = (ApplicationDiscoveryServiceFactory) context.getService(reference);
		System.out.println("===> Bound to ApplicationDiscoveryService with Scope " + scope.getScopeKey());
		ApplicationDiscoveryService discService = discServiceFactory.getServiceForScope(scope);
		try {
			discService.addListener(this);
			return discService;
		} catch (PackagerException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public void removedService(ServiceReference reference, Object service) {
		System.out.println("===> Unbinding ApplicationDiscoveryService with Scope " + scope.getScopeKey());
		ApplicationDiscoveryService discService = (ApplicationDiscoveryService) service;
		discService.removeListener(this);
		
		synchronized (this) {
			for (Entry<String, ServiceRegistration> entry : registrations.entrySet()) {
				System.out.println("===> Unpublishing PublishedApplication service for URI " + entry.getKey());
				entry.getValue().unregister();
			}
		}
	}

	@Override
	public synchronized void serviceReferenceChange(ApplicationDiscoveryEvent event) throws Exception {
		System.out.printf("===> Received service reference change event with %d URIs.\n", event.getServiceReferences().size());
		Set<String> currentUris = new HashSet<String>(registrations.keySet());
		
		Set<ApplicationServiceReference> appRefs = event.getServiceReferences();
		for (ApplicationServiceReference appRef : appRefs) {
			String appUri = appRef.getUri();
			System.out.println("===>    URI = " + appUri);

			boolean exists = currentUris.remove(appUri);
			if (!exists) {
				// publish service
				System.out.println("===> Publishing PublishedApplication service for URI " + appUri);
				Properties serviceProps = new Properties();
				serviceProps.put(PublishedApplication.PROP_URI, appUri);
				PublishedApplication markerService = new PublishedApplication() {};
				ServiceRegistration registration = context.registerService(PublishedApplication.class.getName(), markerService, serviceProps);
				registrations.put(appUri, registration);
			}
		}
		
		// Unpublish services for URIs that no longer exist (i.e. left in the currentUris set)
		for (String uri : currentUris) {
			ServiceRegistration registration = registrations.remove(uri);
			if (registration != null) {
				System.out.println("===> Unpublishing PublishedApplication service for URI " + uri);
				registration.unregister();
			}
		}
	}
	
}
