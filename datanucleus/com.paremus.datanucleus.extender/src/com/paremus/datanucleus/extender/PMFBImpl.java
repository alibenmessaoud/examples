package com.paremus.datanucleus.extender;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

import org.datanucleus.plugin.OSGiPluginRegistry;
import org.osgi.framework.Bundle;

import com.paremus.datanucleus.api.PersistenceManagerFactoryBuilder;

public class PMFBImpl implements PersistenceManagerFactoryBuilder {
	
	private final BundleClassLoader bundleClassLoader;
	private final Map<Object, Object> initialProps;

	public PMFBImpl(Bundle modelBundle, Properties modelProps) {
		bundleClassLoader = new BundleClassLoader(modelBundle, PMFBImpl.class.getClassLoader());
		
		initialProps = new HashMap<Object, Object>();
		initialProps.putAll(modelProps);
		
		initialProps.put("datanucleus.primaryClassLoader", bundleClassLoader);
		initialProps.put("datanucleus.plugin.pluginRegistryClassName", OSGiPluginRegistry.class.getName());
		initialProps.put("datanucleus.autoCreateSchema", "true");
		initialProps.put("datanucleus.validateTables", "false");
		initialProps.put("datanucleus.validateConstraints", false);
	}

	public PersistenceManagerFactory createPersistenceManagerFactory(Map<String, Object> configProps) throws Exception {
		Map<Object, Object> pmfProps = new HashMap<Object, Object>(initialProps);
		pmfProps.putAll(configProps);
		
		PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory(pmfProps, bundleClassLoader);
		return pmf;
	}
	

}
