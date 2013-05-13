package com.paremus.packager.pkg.jbossas.impl;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;

import org.bndtools.service.packager.PackageDescriptor;
import org.bndtools.service.packager.PackageType;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.lib.converter.Converter;
import aQute.lib.io.IO;
import aQute.libg.command.Command;

@Component(properties = {PackageType.PACKAGE_TYPE + "=jbossas", 
		PackageType.VERSION + "=7.1.1.FINAL"})
public class JBossASPackager implements PackageType {
	BundleContext	context;
	Object			lock	= new Object();
	File			manifest;
	Version			jbossVersion = new Version(7, 1, 1, "FINAL");
	boolean			isWindows;

	@Activate
	void activate(BundleContext context) {
		this.context = context;
		// TODO use multiple areas for rollback and
		// and update time
		this.isWindows = context.getProperty(Constants.FRAMEWORK_OS_NAME)
				.toLowerCase().startsWith("windows");
	}

	interface JBossASSetup {
		String appSymbolicName();
		Version appVersion();
		String pathToBinary();
	}

	public PackageDescriptor create(Map<String,Object> properties, File data) throws Exception {
		
		JBossASSetup jbossConfig = Converter.cnv(JBossASSetup.class, properties);

		File inited = new File(data, "inited");
		if (!inited.isFile()) {
			extractJBoss(data, inited);
			extractApplication(data, jbossConfig);
			IO.store(new Date().toString(), inited);
		}

		PackageDescriptor pd = new PackageDescriptor();
		pd.description = "JBoss AS version " + jbossVersion;

		StringBuilder sb = new StringBuilder(data.getAbsolutePath())
				.append("/bin/standalone")
				.append(isWindows ? ".bat" : ".sh");
		

		pd.startScript = sb.toString();
		
		pd.stopScript = new StringBuilder(data.getAbsolutePath())
				.append("/bin/jboss-cli")
				.append(isWindows ? ".bat" : ".sh")
				.append(" --connect command=:shutdown").toString();

		pd.statusScript = new StringBuilder(data.getAbsolutePath())
			.append("/bin/jboss-cli")
			.append(isWindows ? ".bat" : ".sh")
			
		.append(" -c --commands=\"read-attribute server-state\"").toString();

		return pd;
	}

	private void extractJBoss(File data, File inited) throws IOException,
			Exception {
		Bundle b = context.getBundle();
		copy(b, "data", data);
		if(!isWindows) {
			for (File sub : new File(data, "bin").listFiles()) {
				run("chmod a+x " + sub.getAbsolutePath());
			}
		}
	}

	private void extractApplication(File data, JBossASSetup jbossConfig) throws IOException {
		Bundle app = null;
		for(Bundle b : context.getBundles()) {
			if(b.getSymbolicName().equals(jbossConfig.appSymbolicName())
					&& b.getVersion().equals(b.getVersion())) {
				app = b;
				break;
			}
		}
		
		if(app == null)
			throw new IllegalStateException("Unable to locate the application " + 
					jbossConfig.appSymbolicName() + '_' + jbossConfig.appVersion());
		
		copy(app, jbossConfig.pathToBinary(), new File(data, "standalone/deployments"));
		
	}

	private void run(String string) throws Exception {
		Command command = new Command("sh");
		StringBuilder out = new StringBuilder();
		StringBuilder err = new StringBuilder();
		int execute = command.execute(string, out, err);
		if (execute == 0)
			return;

		throw new Exception("command failed " + string + " : " + out + " : " + err);
	}

	private void copy(Bundle b, String path, File data) throws IOException {
		Enumeration<String> e = b.getEntryPaths(path);
		while (e != null && e.hasMoreElements()) {
			String from = e.nextElement();
			if (from.endsWith("/")) {
				String last = from.substring(0, from.length() - 1);
				int n = last.lastIndexOf('/');
				File dir = new File(data, last.substring(n + 1));
				dir.mkdirs();
				copy(b, from, dir);
			} else {
				String last = from.substring(0, from.length());
				int n = last.lastIndexOf('/');
				File to = new File(data, last.substring(n + 1));
				IO.copy(b.getResource(from), to);
			}
		}

	}
}
