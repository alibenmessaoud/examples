package com.paremus.service.javaee;

import org.bndtools.service.packager.PackagerStandardProperties;
import org.osgi.framework.Version;

import aQute.bnd.annotation.metatype.Meta;

public interface JavaEEProperties extends PackagerStandardProperties {
	
	public static final String APP_SYMBOLIC_NAME = "app.symbolic.name";
	public static final String APP_VERSION = "app.version";
	public static final String APP_BUNDLE_ID = "app.bundle.id";
	public static final String CONTEXT_ROOTS = JavaEENamespace.CONTEXT_ROOTS_ATTRIBUTE;
	public static final String PATH_TO_BINARIES = JavaEENamespace.PATH_TO_BINARIES_ATTRIBUTE;
	public static final String DEFAULT_PATH_TO_BINARIES = "javaee";
	
	
	
	String app_symbolic_name();
	Version app_version();
	Long app_bundle_id();
	@Meta.AD(required = false, deflt = "javaee")
	String path_to_binary();
	@Meta.AD(required = false, deflt = "")
	String[] context_roots();
}
