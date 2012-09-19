package com.paremus.packager.whiteboard;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTracker;

public class ScopeDeclarationTracker extends BundleTracker {
	
	public static final String HEADER = "ParemusPackager-Scope";

	public ScopeDeclarationTracker(BundleContext context) {
		super(context, Bundle.STARTING | Bundle.ACTIVE, null);
	}
	
	@Override
	
	public Object addingBundle(Bundle bundle, BundleEvent event) {
		String scopeName = (String) bundle.getHeaders().get(HEADER);
		if (scopeName == null)
			return null;
		
		AppDiscFactoryTracker factoryTracker = new AppDiscFactoryTracker(context, scopeName);
		factoryTracker.open();
		
		return factoryTracker;
	}
	
	@Override
	public void removedBundle(Bundle bundle, BundleEvent event, Object object) {
		AppDiscFactoryTracker tracker = (AppDiscFactoryTracker) object;
		tracker.close();
	}

}
