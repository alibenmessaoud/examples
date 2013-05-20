package com.paremus.packager.pkg.javaee;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

import org.bndtools.service.packager.PackageType;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

import aQute.lib.io.IO;
import aQute.libg.command.Command;

import com.paremus.service.javaee.JavaEEProperties;

public abstract class AbstractJavaEEPackager implements PackageType {
	protected BundleContext context;
	protected boolean isWindows;

	protected void activate(BundleContext context) {
		this.context = context;
		this.isWindows = context.getProperty(Constants.FRAMEWORK_OS_NAME)
				.toLowerCase().startsWith("windows");
	}

	protected File getExtractFolder(File data, JavaEEProperties javaEESetup) throws Exception {
		data = new File(data, javaEESetup.app_symbolic_name() + '/' + javaEESetup.app_version());
		
		if(!data.isDirectory() && !data.mkdirs())
			throw new IOException("Unable to create an extract for the application " 
					+ javaEESetup.app_symbolic_name() + '/' + javaEESetup.app_version());
		return data;
    }
	
	protected void extractServer(File extractFolder, String pathWithinBundle, String pathToBinFolder) 
			throws IOException, Exception {
		Bundle b = context.getBundle();
		copy(b, "data", extractFolder);
		if(!isWindows) {
			for (File sub : new File(extractFolder, pathToBinFolder).listFiles()) {
				chmod(sub.getAbsolutePath());
			}
		}
	}

	protected void extractApplication(File deploymentFolder, JavaEEProperties javaeeConfig) throws IOException {
		Bundle app = null;
		
		if(javaeeConfig.app_bundle_id() != null) {
			app = context.getBundle(javaeeConfig.app_bundle_id());
		} else {
			for(Bundle b : context.getBundles()) {
				if(b.getSymbolicName().equals(javaeeConfig.app_symbolic_name())
						&& b.getVersion().equals(b.getVersion())) {
					app = b;
					break;
				}
			}
		}
		
		if(app == null)
			throw new IllegalStateException("Unable to locate the application " + 
					javaeeConfig.app_symbolic_name() + '_' + javaeeConfig.app_version());
		
		copy(app, javaeeConfig.path_to_binary(), deploymentFolder);
		
	}

	private void chmod(String fileName) throws Exception {
		Command command = new Command("sh");
		StringBuilder out = new StringBuilder();
		StringBuilder err = new StringBuilder();
		int execute = command.execute("chmod a+x \"" + fileName + "\"", out, err);
		if (execute == 0)
			return;

		throw new Exception("command failed " + "chmod a+x " + fileName + " : " + out + " : " + err);
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
