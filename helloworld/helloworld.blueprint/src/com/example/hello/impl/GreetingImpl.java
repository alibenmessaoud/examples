package com.example.hello.impl;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.example.hello.Greeting;

public class GreetingImpl implements Greeting {
	
	private static final String RESOURCE_BASE = "com.example.hello.impl.greetings";
	
	private ResourceBundle resources = ResourceBundle.getBundle(RESOURCE_BASE, Locale.ENGLISH, GreetingImpl.class.getClassLoader());
	
	public void setLanguage(String language) {
		System.out.println("-------> Setting language to " + language);
		try {
			resources = ResourceBundle.getBundle(RESOURCE_BASE, new Locale(language), GreetingImpl.class.getClassLoader());
		} catch (MissingResourceException e) {
			System.err.println("-------> No greeting available for language: " + language);
		}
	}

	@Override
	public void sayHello(String name) {
		String message = MessageFormat.format(resources.getString("greeting"), name);
		System.out.println(message);
	}
	
	public void refresh() {
		System.out.println("-------> Configuration updated.");
	}

}
