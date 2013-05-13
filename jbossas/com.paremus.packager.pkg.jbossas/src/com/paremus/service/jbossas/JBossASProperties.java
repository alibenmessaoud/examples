package com.paremus.service.jbossas;

import org.bndtools.service.packager.PackagerStandardProperties;
import org.osgi.framework.Version;

import aQute.bnd.annotation.metatype.Meta;

public interface JBossASProperties extends PackagerStandardProperties {
	
	String appSymbolicName();
	Version appVersion();
	@Meta.AD(required = false, deflt = "javaee")
	String pathToBinary();
	@Meta.AD(required = false, deflt = "")
	String[] contextRoots();
}
