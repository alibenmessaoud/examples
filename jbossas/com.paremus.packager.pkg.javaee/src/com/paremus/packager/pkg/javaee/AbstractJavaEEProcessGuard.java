package com.paremus.packager.pkg.javaee;

import java.net.InetAddress;
import java.net.URI;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import org.bndtools.service.endpoint.Endpoint;
import org.bndtools.service.packager.ProcessGuard;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.remoteserviceadmin.RemoteConstants;

import com.paremus.service.javaee.JavaEEProperties;

public abstract class AbstractJavaEEProcessGuard implements ProcessGuard {
	
	protected JavaEEProperties config;
	Map<String, Object> configProps;
	BundleContext context;
	
	private ServiceRegistration<Endpoint> registration;

	protected void activate(BundleContext context, JavaEEProperties javaEEProperties, Map<String,Object> configProps) throws Exception {
		this.config = javaEEProperties;
		this.context = context;
		this.configProps = configProps;
	}

	public Map<String,Object> getProperties() {
		return configProps;
	}

	public synchronized void state(State state) throws Exception {
		if (state.isAlive()) {
			if (registration == null) {
				Dictionary<String,Object> props = new Hashtable<String, Object>();
				
				String hostName = InetAddress.getLocalHost().getHostAddress();
				String[] contextRoots = config.contextRoots();
				URI[] uris = new URI[contextRoots.length];
				
				for(int i = 0; i < uris.length; i ++) {
					uris[i] = new URI("http", null, hostName, 8080, 
							contextRoots[i].charAt(0) == '/' ? contextRoots[i] : "/" + contextRoots[i],
							null, null);
				}
				
				props.put(RemoteConstants.SERVICE_EXPORTED_INTERFACES, "*");
				props.put("app.symbolic.name", config.appSymbolicName());
				props.put("app.version", config.appVersion());
				props.put("availableURIs", uris);
				registration = context.registerService(Endpoint.class, new Endpoint() {}, props);
			}
		} else if (registration != null) {
			registration.unregister();
			registration = null;
		}
	}
}
