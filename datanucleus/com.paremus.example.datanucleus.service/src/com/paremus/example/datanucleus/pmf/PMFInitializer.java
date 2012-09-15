package com.paremus.example.datanucleus.pmf;

import java.util.HashMap;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

import org.datanucleus.plugin.OSGiPluginRegistry;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;

@Component
public class PMFInitializer {

	private static final ClassLoader LOADER = PMFInitializer.class.getClassLoader();
	
	private PersistenceManagerFactory pmf;
	private ServiceRegistration reg;

	@Activate
	public void activate(BundleContext context) {
		Map<String, Object> props = new HashMap<String, Object>();
		
		props.put("datanucleus.primaryClassLoader", LOADER);
		props.put("datanucleus.plugin.pluginRegistryClassName", OSGiPluginRegistry.class.getName());
		props.put("datanucleus.autoCreateSchema", "true");
		props.put("datanucleus.validateTables", "false");
		props.put("datanucleus.validateConstraints", false);
		
		props.put("javax.jdo.PersistenceManagerFactoryClass", "org.datanucleus.api.jdo.JDOPersistenceManagerFactory");
		props.put("javax.jdo.option.ConnectionURL", "jdbc:derby://localhost/datanucleussample;create=true");
		props.put("javax.jdo.option.ConnectionDriverName", "org.apache.derby.jdbc.ClientDriver");
		props.put("javax.jdo.option.ConnectionUserName", "APP");
		props.put("javax.jdo.option.ConnectionPassword", "APP");
		
		pmf = JDOHelper.getPersistenceManagerFactory(props, LOADER);
		
		reg = context.registerService(PersistenceManagerFactory.class.getName(), pmf, null);
	}

	@Deactivate
	public void deactivate() {
		reg.unregister();
		pmf.close();
	}

}
