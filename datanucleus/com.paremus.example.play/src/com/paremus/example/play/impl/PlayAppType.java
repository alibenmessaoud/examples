package com.paremus.example.play.impl;

import java.io.File;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;

import org.bndtools.service.packager.PackageDescriptor;
import org.bndtools.service.packager.PackageType;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.metatype.Configurable;
import aQute.lib.io.IO;

import com.paremus.example.play.PlayAppProperties;

@Component(properties = "package.type=play-rest")
public class PlayAppType implements PackageType {
	
	private BundleContext context;

	@Activate
	void activate(BundleContext context) {
		this.context = context;
	}

	@Override
	public PackageDescriptor create(Map<String, Object> properties, File data) throws Exception {
		PackageDescriptor pd = new PackageDescriptor();

		// Copy the JARs to the install area if they don't currently exist
		PlayAppProperties config = Configurable.createConfigurable(PlayAppProperties.class, properties);
		File stagedDir = new File(data, "staged");
		if (stagedDir.mkdirs()) {
			copy(context.getBundle(), "data", stagedDir);
		}
		if (!stagedDir.isDirectory())
			throw new IllegalArgumentException("Failed to install Play REST staged directory");
		
		// Build the Java application classpath
		StringBuilder classpath = new StringBuilder();
		File[] children = stagedDir.listFiles();
		String del = "";
		if (children != null) for (File child : children) {
			classpath.append(del).append(child.getAbsolutePath());
			del = ":";
		}
		
		// Build the scripts
		pd.startScript = String.format("export SERVICE_URL_MIDTIER=%s %n exec java -cp \"%s\" -Dhttp.port=%d play.core.server.NettyServer",
				"midtier:" + config.restUrl(), classpath, config.httpPort());
		pd.description = "Play REST Application";
		
		return pd;
	}

	private void copy(Bundle b, String path, File data) throws IOException {
		@SuppressWarnings("unchecked")
		Enumeration<String> e = (Enumeration<String>) b.getEntryPaths(path);
		while (e.hasMoreElements()) {
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
