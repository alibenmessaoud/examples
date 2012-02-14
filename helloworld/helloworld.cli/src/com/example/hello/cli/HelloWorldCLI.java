package com.example.hello.cli;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.example.hello.Greeting;

public class HelloWorldCLI {
	
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
	
	public void sayHello(String[] args) {
		if (args == null || args.length < 1)
			throw new IllegalArgumentException("Missing name argument");
		
		Greeting greeting = (Greeting) greetingTracker.getService();
		if (greeting == null)
			System.err.println("Greeting service not available");
		else
			greeting.sayHello(args[0]);
	}
	
	
}
