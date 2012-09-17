package com.paremus.datanucleus.extender;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.paremus.internal.util.log.LogServiceTracker;

public class Activator implements BundleActivator {

	private LogServiceTracker log;
	private DataNucleusModelBundleTracker extender;

	@Override
	public void start(BundleContext context) throws Exception {
		log = new LogServiceTracker(context);
		log.open();
		
		extender = new DataNucleusModelBundleTracker(context, log);
		extender.open();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		extender.close();
		log.close();
	}

}
