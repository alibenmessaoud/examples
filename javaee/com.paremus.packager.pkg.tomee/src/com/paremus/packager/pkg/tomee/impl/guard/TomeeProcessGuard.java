package com.paremus.packager.pkg.tomee.impl.guard;

import java.util.Map;

import org.bndtools.service.packager.ProcessGuard;
import org.osgi.framework.BundleContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.metatype.Configurable;

import com.paremus.packager.pkg.javaee.AbstractJavaEEProcessGuard;
import com.paremus.service.javaee.JavaEEProperties;
import com.paremus.service.tomee.TomeeProperties;

@Component(
		name = "com.paremus.packager.pkg.tomee",
		designateFactory = JavaEEProperties.class,
		properties = {
			ProcessGuard.PACKAGE_TYPE + "=" + TomeeProperties.TOMEE,
			ProcessGuard.VERSION + "=1.5.2"
		})
public class TomeeProcessGuard extends AbstractJavaEEProcessGuard implements ProcessGuard {
	
	TomeeProperties config;

	@Activate
	void activate(BundleContext context, Map<String,Object> configProps) throws Exception {
		this.config = Configurable.createConfigurable(TomeeProperties.class, configProps);
		activate(context, config, configProps);
	}
  }
