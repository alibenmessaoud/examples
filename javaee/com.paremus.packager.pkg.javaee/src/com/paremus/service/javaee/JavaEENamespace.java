package com.paremus.service.javaee;

import org.osgi.resource.Namespace;

public class JavaEENamespace extends Namespace {

	public static final String JAVA_EE_NAMESPACE = "com.paremus.javaee";

	public static final String PATH_TO_BINARIES_ATTRIBUTE = "path.to.binaries";
	public static final String CONTEXT_ROOTS_ATTRIBUTE = "context.roots";
	
	public static final String ENDPOINT_TYPE_SERVICE_PROPERTY_NAME = "type"; 
	public static final String ENDPOINT_TYPE_DATABASE = "database"; 
	
	public static final String JAVA_EE_DB_NAMESPACE = "com.paremus.javaee.db";
	
}
