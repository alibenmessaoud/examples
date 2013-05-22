package com.paremus.packager.pkg.tomee.impl;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

import org.bndtools.service.packager.PackageDescriptor;
import org.bndtools.service.packager.PackageType;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.lib.converter.Converter;
import aQute.lib.io.IO;

import com.paremus.packager.pkg.javaee.AbstractJavaEEPackager;
import com.paremus.service.tomee.TomeeProperties;

@Component(properties = {PackageType.PACKAGE_TYPE + "=" + TomeeProperties.TOMEE, 
		PackageType.VERSION + "=1.5.2"})
public class TomeePackager extends AbstractJavaEEPackager implements PackageType {

	Version tomeeVersion = new Version(1, 5, 2);
	
	@Activate
	protected void activate(BundleContext context) {
		super.activate(context);
	}

	interface TomeeSetup extends JavaEESetup {}

	public PackageDescriptor create(Map<String,Object> properties, File data) throws Exception {
		
		TomeeSetup tomeeConfig = Converter.cnv(TomeeSetup.class, properties); 
		
		File extractFolder = getExtractFolder(data, tomeeConfig);
		
		File inited = new File(extractFolder, "inited");
		if (!inited.isFile()) {
			extractServer(extractFolder, "data", "bin");
			File deploymentFolder = new File(extractFolder, "apps");
			if(!deploymentFolder.isDirectory() && !deploymentFolder.mkdirs())
				throw new IOException("Unable to deploy applications from " + 
						tomeeConfig.appSymbolicName() + '_' + tomeeConfig.appVersion());
			extractApplication(deploymentFolder, tomeeConfig);
			IO.store(new Date().toString(), inited);
		}

		String setJAVA_HOME = new StringBuilder(isWindows? "set" : "export")
			.append(" JAVA_HOME=")
			.append(System.getProperty("java.home"))
			.append("\n")
			.toString();
		
		
		PackageDescriptor pd = new PackageDescriptor();
		pd.description = "Tomee version " + tomeeVersion;

		pd.startScript = new StringBuilder(setJAVA_HOME)
				.append("\"")
				.append(extractFolder.getAbsolutePath())
				.append("/bin/catalina")
				.append(isWindows ? ".bat" : ".sh")
				.append("\"")
				.append(" run")
				.toString();
		
		pd.stopScript = new StringBuilder(setJAVA_HOME)
				.append("\"")
				.append(extractFolder.getAbsolutePath())
				.append("/bin/catalina")
				.append(isWindows ? ".bat" : ".sh")
				.append("\"")
				.append(" stop")
				.toString();

		if(isWindows) {
			pd.statusScript = new StringBuilder("wmic process list | find /C /I \"-Dcatalina.home=")
				.append(extractFolder.getAbsolutePath())
				.append("\"")
				.toString();
		} else {
			pd.statusScript = new StringBuilder("ps -a | grep \"\\-Dcatalina.home=")
				.append(extractFolder.getAbsolutePath())
				.append("\"")
				.toString();
		}

		return pd;
	}
}
