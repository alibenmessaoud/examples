package com.paremus.packager.pkg.jbossas.impl.guard;

import java.util.Map;

import org.bndtools.service.packager.ProcessGuard;
import org.osgi.framework.BundleContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.metatype.Configurable;

import com.paremus.packager.pkg.javaee.AbstractJavaEEProcessGuard;
import com.paremus.service.javaee.JavaEEProperties;
import com.paremus.service.jbossas.JBossASProperties;

@Component(
		name = "com.paremus.packager.pkg.jbossas",
		designateFactory = JavaEEProperties.class,
		properties = {
			ProcessGuard.PACKAGE_TYPE + "=" + JBossASProperties.JBOSSAS, 
			ProcessGuard.VERSION + "=7.1.1.FINAL"
		})
public class JBossASProcessGuard extends AbstractJavaEEProcessGuard implements ProcessGuard {
	
	JBossASProperties config;
	
	@Activate
	void activate(BundleContext context, Map<String,Object> configProps) throws Exception {
		this.config = Configurable.createConfigurable(JBossASProperties.class, configProps);
		activate(context, config, configProps);
	}

}
