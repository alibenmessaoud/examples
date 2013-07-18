package com.example.hello.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.example.hello.Greeting;

public class GreetingActivator implements BundleActivator {

	public void start(BundleContext context) throws Exception {
		context.registerService(Greeting.class.getName(), new GreetingImpl(), null);
	}

	public void stop(BundleContext context) throws Exception {
	}

}
