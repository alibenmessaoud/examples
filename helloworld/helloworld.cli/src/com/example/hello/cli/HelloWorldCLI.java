package com.example.hello.cli;

import java.util.Dictionary;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.service.cm.ManagedService;

import com.example.hello.Greeting;

public class HelloWorldCLI implements ManagedService {
	
	static final String SCOPE = "helloworld";
	static final String[] FUNCTIONS = { "sayHello" };
	
	private ServiceTracker greetingTracker;
	
	synchronized void start(BundleContext context) {
		greetingTracker = new ServiceTracker(context, Greeting.class.getName(), null);
		greetingTracker.open();
	}
	
	synchronized void stop() {
		greetingTracker.close();
		greetingTracker = null;
	}
	
	private volatile boolean preferLocal = false;
	
	public void updated(Dictionary properties) {
		
		if(preferLocal = properties != null && Boolean.parseBoolean(properties.get("prefer.local").toString())) {
			System.out.println("-------> Prefer local services");
		} else {
			System.out.println("-------> Use standard OSGi service selection rules");
		}
	}
	
	public void sayHello(String[] args) {
		if (args == null || args.length < 1)
			throw new IllegalArgumentException("Missing name argument");

		Greeting greeting = (Greeting) (preferLocal ? findLocal() : greetingTracker.getService());
		if (greeting == null)
			System.err.println("Greeting service not available");
		else
			greeting.sayHello(args[0]);
	}
	
	private Object findLocal() {
		for(ServiceReference sr : greetingTracker.getServiceReferences()) {
			if(sr.getProperty("service.imported") == null) {
				return greetingTracker.getService(sr);
			}
		}
		return greetingTracker.getService();
	}
}
