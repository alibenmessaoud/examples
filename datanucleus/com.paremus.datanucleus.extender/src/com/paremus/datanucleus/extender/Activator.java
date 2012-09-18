package com.paremus.datanucleus.extender;

import org.datanucleus.plugin.OSGiPluginRegistry;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleReference;
import org.osgi.service.log.LogService;

import com.paremus.internal.util.log.LogServiceTracker;

public class Activator implements BundleActivator {

	private LogServiceTracker log;
	private DataNucleusModelBundleTracker extender;

	@Override
	public void start(BundleContext context) throws Exception {
		log = new LogServiceTracker(context);
		log.open();
		
		// Ensure the org.datanucleus bundle is started, otherwise the DataNucleus plugin registry will throw NPEs
		ClassLoader registryClassLoader = OSGiPluginRegistry.class.getClassLoader();
		if (registryClassLoader instanceof BundleReference) {
			Bundle registryBundle = ((BundleReference) registryClassLoader).getBundle();
			registryBundle.start();
		} else {
			log.log(LogService.LOG_ERROR, "OSGiPluginRegisty class was not loaded by an OSGi ClassLoader.");
		}
		
		extender = new DataNucleusModelBundleTracker(context, log);
		extender.open();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		extender.close();
		log.close();
	}

}
