package com.example.hello.impl;


import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.ConfigurationPolicy;
import aQute.bnd.annotation.metatype.Configurable;
import aQute.bnd.annotation.metatype.Meta;

import com.example.hello.Greeting;

@Component(
		name = "com.example.hello.ds",
		designate = GreetingImpl.Config.class,
		configurationPolicy = ConfigurationPolicy.optional)
public class GreetingImpl implements Greeting {
	
	interface Config {
		@Meta.AD(required = false, deflt = "en")
		String language();
	}
	
	private static final String RESOURCE_BASE = "com.example.hello.impl.greetings";
	
	private ResourceBundle resources;
	
	@Activate
	public void activate(Map<String, Object> configProps) {
		Config config = Configurable.createConfigurable(Config.class, configProps);
		
		try {
			System.out.println("-------> Setting language to " + config.language());
			Locale locale = config.language() == null ? Locale.getDefault() : new Locale(config.language());
			resources = ResourceBundle.getBundle(RESOURCE_BASE, locale);
		} catch (MissingResourceException e) {
			System.err.println("-------> No greeting available for language: " + config.language());
		}
	}

	@Override
	public void sayHello(String name) {
		String message = MessageFormat.format(resources.getString("greeting"), name);
		System.out.println(message);
	}

}
