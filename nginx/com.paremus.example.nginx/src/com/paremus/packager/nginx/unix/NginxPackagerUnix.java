package com.paremus.packager.nginx.unix;

import java.io.File;
import java.io.FileWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Map;

import org.bndtools.service.packager.PackageDescriptor;
import org.bndtools.service.packager.PackageType;

import com.paremus.service.nginx.NginxProperties;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.metatype.Configurable;
import aQute.lib.io.IO;
import aQute.libg.command.Command;

@Component(properties = {PackageType.PACKAGE_TYPE + "=nginx", 
		PackageType.VERSION + "=1.2.8"})
public class NginxPackagerUnix implements PackageType {
	
	private final static String[] sourceFiles = new String[] {
		"nginx", "mime.types", "lib/libcrypto.0.9.8.dylib", "lib/libpcre.1.dylib",
		"lib/libssl.0.9.8.dylib", "lib/libSystem.B.dylib", "lib/libz.1.dylib" };

	@Override
	public PackageDescriptor create(Map<String, Object> properties, File data)
			throws Exception {
		NginxProperties config = Configurable.createConfigurable(NginxProperties.class, properties);
		File[] destFiles = copyFilesToDir(sourceFiles, "/data", data);
		for (File file : destFiles) {
			if (!file.getName().equals("mime.types"))
				run("chmod a+x " + file.getAbsolutePath());
		}
		File logsDir = new File(data, "logs");
		if (!logsDir.exists()) {
			boolean created = logsDir.mkdir();
			if (!created)
				throw new Exception("directory creation failed : " + logsDir.getAbsolutePath());
		}
		File nginxConf = new File(data, "nginx.conf");
		IO.copy(new StringReader(generateConfigFile(config)), new FileWriter(nginxConf));
		String nginxPath = new File(data, "nginx").getAbsolutePath();
		String libPath = new File(data, "lib").getAbsolutePath();
		String confPath = nginxConf.getAbsolutePath();
		PackageDescriptor pd = new PackageDescriptor();
		StringBuilder sb = new StringBuilder();
		sb.append("export DYLD_LIBRARY_PATH=").append(libPath).append("\n");
		sb.append(nginxPath).append(" -p " + data.getAbsolutePath());
		sb.append(" -c " + confPath);
		sb.append(" -g \"daemon off;\"");
		pd.startScript = sb.toString();
		sb = new StringBuilder();
		sb.append(nginxPath).append(" -s stop");
		pd.stopScript = sb.toString();
		sb = new StringBuilder();
		sb.append("ps x | grep nginx | grep 'master process'");
		pd.statusScript = sb.toString();
		pd.description = "Nginx Packager";
		return pd;
	}
	
	private File[] copyFilesToDir(String[] fileNames, String sourcePath, File destPath) throws Exception {
		ArrayList<File> destFiles = new ArrayList<File>();
		for (String fileName : fileNames) {
			File destDir = new File(destPath, fileName).getParentFile();
			if (destDir != null && !destDir.exists()) {
				boolean created = destDir.mkdir();
				if (!created)
					throw new Exception("directory creation failed : " + destDir.getAbsolutePath());
			}
			File dest = new File(destPath, fileName);
			if (!dest.exists())
				IO.copy(getClass().getResource(sourcePath + "/" + fileName), dest);
			destFiles.add(dest);
		}
		return destFiles.toArray(new File[0]);
	}
	
	private String generateConfigFile(NginxProperties properties) {
		StringBuilder sb = new StringBuilder();
		
		if (properties.username() != null && !"".equals(properties.username().trim())) {
			sb.append("user  ").append(properties.username()).append(";\n");
		}
		sb.append("worker_processes  ").append(properties.workerProcesses()).append(";\n");
		sb.append("events {\n");
		sb.append("    worker_connections  ").append(properties.workerConnections()).append(";\n");
		sb.append("}\n");
		sb.append("http {\n");
		sb.append("    include       mime.types;\n");
		sb.append("    default_type  application/octet-stream;\n");
		sb.append("    server {\n");
		sb.append("        listen       ").append(properties.listen()).append(";\n");;
		sb.append("        server_name  ").append(properties.serverName()).append(";\n");
		sb.append("        root         ").append(properties.root()).append(";\n");
		sb.append("        location / {\n");
		sb.append("            proxy_pass          ").append(properties.proxyPass()).append(";\n");
		sb.append("            proxy_set_header    X-Real-IP $remote_addr;\n");
		sb.append("            proxy_set_header    X-Forwarded-For $proxy_add_x_forwarded_for;\n");
		sb.append("            proxy_set_header    Host $http_host;\n");
		sb.append("        }\n");
		sb.append("    }\n");
		sb.append("}\n");
		return sb.toString();
	}

	private void run(String string) throws Exception {
		Command command = new Command("sh");
		StringBuilder out = new StringBuilder();
		StringBuilder err = new StringBuilder();
		int execute = command.execute(string, out, err);
		if (execute == 0) return;
		throw new Exception("command failed " + string + " : " + out + " : " + err);
	}

}
