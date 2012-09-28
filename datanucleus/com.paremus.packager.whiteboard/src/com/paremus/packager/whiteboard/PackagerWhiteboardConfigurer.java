package com.paremus.packager.whiteboard;

import java.util.Map;
import java.util.Properties;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;
import aQute.bnd.annotation.metatype.Configurable;
import aQute.bnd.annotation.metatype.Meta;

import com.paremus.packager.api.PackagerException;
import com.paremus.packager.api.Scope;
import com.paremus.packager.api.discovery.ApplicationDiscoveryService;
import com.paremus.packager.api.discovery.ApplicationDiscoveryServiceFactory;

@Component(name = "com.paremus.packager.whiteboard", designateFactory = PackagerWhiteboardConfigurer.Config.class)
public class PackagerWhiteboardConfigurer {

	interface Config {
		@Meta.AD(description = "Packager scope, e.g. 'global'")
		String scope();
	}
	
	private ApplicationDiscoveryServiceFactory appDiscSvcFactory;
	
	@Reference
	public void setApplicationDiscoveryServiceFactory(ApplicationDiscoveryServiceFactory appDiscSvcFactory) {
		this.appDiscSvcFactory = appDiscSvcFactory;
	}

	private Scope scope;
	private ApplicationDiscoveryService appDiscSvc;
	private ServiceRegistration registration;
	private Listener listener;
	
	@Activate
	public void activate(BundleContext context, Map<String, Object> configProps) {
		Config config = Configurable.createConfigurable(Config.class, configProps);
		
		System.out.printf("===> Configured packager whiteboard for scope %s.%n", config.scope());
		scope = new Scope(config.scope());
		appDiscSvc = appDiscSvcFactory.getServiceForScope(scope);
		
		System.out.printf("===> Registering ApplicationDiscoveryService for scope %s.%n", scope.getScopeKey());
		Properties regProps = new Properties();
		regProps.put("scope", scope.getScopeKey());
		registration = context.registerService(ApplicationDiscoveryService.class.getName(), appDiscSvc, regProps);
		
		System.out.printf("===> Adding discovery listener for scope %s.%n", scope.getScopeKey());
		listener = new Listener(context, scope);
		try {
			appDiscSvc.addListener(listener);
		} catch (PackagerException e) {
			e.printStackTrace();
		}
	}
	
	@Deactivate
	public void deactivate() {
		System.out.printf("===> Unregistering ApplicationDiscoveryService for scope %s.%n", scope.getScopeKey());
		registration.unregister();
		System.out.printf("===> Shutting down ApplicationDiscoveryService for scope %s.%n", scope.getScopeKey());
		appDiscSvc.shutdown();
	}
	

}
