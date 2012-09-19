package com.paremus.packager.whiteboard;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private ScopeDeclarationTracker tracker;

	@Override
	public void start(BundleContext context) throws Exception {
		tracker = new ScopeDeclarationTracker(context);
		tracker.open();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		tracker.close();
	}

}
