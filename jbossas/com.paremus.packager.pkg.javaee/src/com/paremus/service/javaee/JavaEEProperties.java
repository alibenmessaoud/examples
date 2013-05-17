package com.paremus.service.javaee;

import org.bndtools.service.packager.PackagerStandardProperties;
import org.osgi.framework.Version;

import aQute.bnd.annotation.metatype.Meta;

public interface JavaEEProperties extends PackagerStandardProperties {
	
	String appSymbolicName();
	Version appVersion();
	@Meta.AD(required = false, deflt = "javaee")
	String pathToBinary();
	@Meta.AD(required = false, deflt = "")
	String[] contextRoots();
}
