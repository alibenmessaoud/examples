package com.paremus.packager.pkg.jbossas.impl;

import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationAdmin;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.ConfigurationPolicy;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;

import com.paremus.packager.pkg.javaee.AbstractJavaEEConfigurer;

@Component(configurationPolicy=ConfigurationPolicy.optional)
public class JBossConfigurer extends AbstractJavaEEConfigurer {

	public static final String JBOSS_GUARD_FACTORY_PID = "com.paremus.packager.pkg.jbossas";
	
	private volatile ConfigurationAdmin configAdmin;
	
	public JBossConfigurer() {
		super(JBOSS_GUARD_FACTORY_PID);
	}

	@Reference
	void setConfigAdmin(ConfigurationAdmin configAdmin) {
		this.configAdmin = configAdmin;
	}

	@Activate
	protected synchronized void activate(BundleContext context) {
		super.activate(context, configAdmin);
	}

	@Override
	@Deactivate
	protected synchronized void deactivate() {
		super.deactivate();
	}
}