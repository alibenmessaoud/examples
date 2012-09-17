package com.paremus.datanucleus.extender;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.BundleTracker;

import com.paremus.datanucleus.api.PersistenceManagerFactoryBuilder;

public class DataNucleusModelBundleTracker extends BundleTracker {

	private static final String HEADER_KEY = "DataNucleus-Model";
	private static final String PROPERTIES_ENTRY = "OSGI-INF/datanucleus/model.properties";
	private static final String SERVICE_REGISTRATION_PROP_MODEL = "com.paremus.datanucleus.model";
	
	private LogService log;

	public DataNucleusModelBundleTracker(BundleContext context, LogService log) {
		super(context, Bundle.ACTIVE, null);
		this.log = log;
	}
	
	@Override
	public Object addingBundle(Bundle bundle, BundleEvent event) {
		String modelName = (String) bundle.getHeaders().get(HEADER_KEY);
		if (modelName == null)
			return null;
		
		Properties props = new Properties();
		try {
			loadModelProperties(bundle, props);
			PMFBImpl pmfb = new PMFBImpl(bundle, props);
			
			Properties svcRegProps = new Properties();
			svcRegProps.put(SERVICE_REGISTRATION_PROP_MODEL, modelName);
			
			return context.registerService(PersistenceManagerFactoryBuilder.class.getName(), pmfb, svcRegProps);
		} catch (IOException e) {
			log.log(LogService.LOG_ERROR, String.format("Error loading DataNucleus properties from " + PROPERTIES_ENTRY + " entry in bundle %d.", bundle.getBundleId()), e);
			return null;
		}
	}
	
	private void loadModelProperties(Bundle bundle, Properties properties) throws IOException {
		URL entry = bundle.getEntry(PROPERTIES_ENTRY);
		if (entry != null) {
			InputStream stream = null;
			try {
				stream = entry.openStream();
				properties.load(stream);
			} finally {
				try { if (stream != null) stream.close(); } catch (IOException e) { /* ignore */ }
			}
		}
	}

	@Override
	public void removedBundle(Bundle bundle, BundleEvent event, Object object) {
		((ServiceRegistration) object).unregister();
	}
}
