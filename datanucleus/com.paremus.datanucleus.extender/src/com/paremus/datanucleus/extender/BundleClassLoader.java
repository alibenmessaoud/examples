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
		System.out.println("----------> Trying to load class " + name);
		Class<?> loaded = findLoadedClass(name);
		
		if (loaded != null) {
			System.out.println("----------> Returning already loaded class");
		} else {
			try {
				loaded = super.loadClass(name, false);
			} catch (ClassNotFoundException e) {
				// swallow
			}
			if (loaded == null) {
				try {
					loaded = bundle.loadClass(name);
					System.out.println("----------> Successfully loaded class + " + name);
				} catch (ClassNotFoundException e) {
					System.out.println("----------> ERROR loading class + " + name);
					e.printStackTrace();
					throw e;
				}
			}
		}
		
		if (resolve)
			resolveClass(loaded);
		return loaded;
	}

}
