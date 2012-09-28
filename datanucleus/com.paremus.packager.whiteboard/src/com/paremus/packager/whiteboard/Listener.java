package com.paremus.packager.whiteboard;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.paremus.packager.api.Scope;
import com.paremus.packager.api.discovery.ApplicationDiscoveryEvent;
import com.paremus.packager.api.discovery.ApplicationDiscoveryListener;
import com.paremus.packager.api.discovery.ApplicationServiceReference;
import com.paremus.packager.whiteboard.api.PublishedApplication;

public class Listener implements ApplicationDiscoveryListener {
	
	private final Map<String, ServiceRegistration> registrations = new HashMap<String, ServiceRegistration>();
	
	private final BundleContext context;
	private final Scope scope;

	public Listener(BundleContext context, Scope scope) {
		this.context = context;
		this.scope = scope;
	}

	@Override
	public synchronized void serviceReferenceChange(ApplicationDiscoveryEvent event) throws Exception {
		System.out.printf("===> Received service reference change event with %d URIs in scope %s.%n", event.getServiceReferences().size(), scope.getScopeKey());
		Set<String> currentUris = new HashSet<String>(registrations.keySet());
		
		Set<ApplicationServiceReference> appRefs = event.getServiceReferences();
		for (ApplicationServiceReference appRef : appRefs) {
			String appUri = appRef.getUri();
			System.out.println("===>    URI = " + appUri);

			boolean exists = currentUris.remove(appUri);
			if (!exists) {
				// publish service
				System.out.printf("===> Publishing PublishedApplication service for URI=%s, scope=%s.%n", appUri, scope.getScopeKey());
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
				System.out.printf("===> Unpublishing PublishedApplication service for URI=%s, scope=%s.%n", uri, scope.getScopeKey());
				registration.unregister();
			}
		}
	}
}
