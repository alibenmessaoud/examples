package com.paremus.datanucleus.extender;

import org.osgi.framework.Bundle;

public class BundleClassLoader extends ClassLoader {
	
	private Bundle bundle;

	public BundleClassLoader(Bundle bundle, ClassLoader parent) {
		super(parent);
		this.bundle = bundle;
	}
	
	public BundleClassLoader(Bundle bundle) {
		this.bundle = bundle;
	}
	
	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		Class<?> loaded = findLoadedClass(name);
		
		if (loaded == null) {
			try {
				loaded = super.loadClass(name, false);
			} catch (ClassNotFoundException e) {
				// swallow
			}
			if (loaded == null) {
				loaded = bundle.loadClass(name);
			}
		}
		
		if (resolve)
			resolveClass(loaded);
		return loaded;
	}

}
