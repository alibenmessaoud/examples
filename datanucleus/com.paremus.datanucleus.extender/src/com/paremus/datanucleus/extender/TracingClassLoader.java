package com.paremus.datanucleus.extender;

public class TracingClassLoader extends ClassLoader {

	private ClassLoader delegate;

	public TracingClassLoader(ClassLoader delegate) {
		this.delegate = delegate;
	}

	public Class<?> loadClass(String name) throws ClassNotFoundException {
		System.out.println("----------> Trying to load class " + name);
		try {
			Class<?> loaded = delegate.loadClass(name);
			System.out.println("----------> Successfully loaded class + " + name);
			return loaded;
		} catch (ClassNotFoundException e) {
			System.out.println("----------> ERROR loading class + " + name);
			e.printStackTrace();
			throw e;
		}
	}
	
	
}
