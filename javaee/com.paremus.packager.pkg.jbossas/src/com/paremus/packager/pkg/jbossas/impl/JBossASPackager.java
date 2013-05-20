package com.paremus.packager.pkg.jbossas.impl;

import java.io.File;
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
import com.paremus.service.jbossas.JBossASProperties;

@Component(properties = {
		PackageType.PACKAGE_TYPE + "=" + JBossASProperties.JBOSSAS, 
		PackageType.VERSION + "=7.1.1.FINAL"
	})
public class JBossASPackager extends AbstractJavaEEPackager implements PackageType {

	Version jbossVersion = new Version(7, 1, 1, "FINAL");

	@Activate
	protected void activate(BundleContext context) {
		super.activate(context);
	}

	public PackageDescriptor create(Map<String,Object> properties, File data) throws Exception {
		
		JBossASProperties jbossConfig = Converter.cnv(JBossASProperties.class, properties); 
		
		File extractFolder = getExtractFolder(data, jbossConfig);
		
		File inited = new File(extractFolder, "inited");
		if (!inited.isFile()) {
			extractServer(extractFolder, "data", "bin");
			extractApplication(new File(extractFolder, "standalone/deployments"), jbossConfig);
			IO.store(new Date().toString(), inited);
		}

		String setJAVA_HOME = new StringBuilder(isWindows? "set" : "export")
		.append(" JAVA_HOME=")
		.append(System.getProperty("java.home"))
		.append("\n")
		.toString();
		
		
		PackageDescriptor pd = new PackageDescriptor();
		pd.description = "JBoss AS version " + jbossVersion;

		pd.startScript = new StringBuilder(setJAVA_HOME)
				.append("\"")
				.append(extractFolder.getAbsolutePath())
				.append("/bin/standalone")
				.append(isWindows ? ".bat" : ".sh")
				.append("\"")
				.toString();
		
		pd.stopScript = new StringBuilder(setJAVA_HOME)
				.append("\"")
				.append(extractFolder.getAbsolutePath())
				.append("/bin/jboss-cli")
				.append(isWindows ? ".bat" : ".sh")
				.append("\"")
				.append(" --connect command=:shutdown")
				.toString();

		pd.statusScript = new StringBuilder(setJAVA_HOME)
				.append("\"")
				.append(extractFolder.getAbsolutePath())
				.append("/bin/jboss-cli")
				.append(isWindows ? ".bat" : ".sh")
				.append("\"")
				.append(" -c --commands=\"read-attribute server-state\"")
				.toString();

		return pd;
	}
}
