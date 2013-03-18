package com.paremus.packager.nginx.unix;

import java.io.File;
import java.io.FileWriter;
import java.io.StringReader;
import java.util.Map;

import org.bndtools.service.packager.PackageDescriptor;
import org.bndtools.service.packager.PackageType;

import com.paremus.service.nginx.NginxProperties;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.metatype.Configurable;
import aQute.lib.io.IO;
import aQute.libg.command.Command;

@Component(properties = "package.type=nginx")
public class NginxPackagerUnix implements PackageType {

	@Override
	public PackageDescriptor create(Map<String, Object> properties, File data)
			throws Exception {
		NginxProperties config = Configurable.createConfigurable(NginxProperties.class, properties);
		PackageDescriptor pd = new PackageDescriptor();
		File nginx = new File(data, "nginx");
		if (!nginx.isFile()) {
			IO.copy(getClass().getResource("/data/nginx"), nginx);
			run("chmod a+x " + nginx.getAbsolutePath());	
		}
		File configDir = new File(data, "config");
		if (!configDir.isDirectory()) {
			boolean created = configDir.mkdir();
			if (!created)
				throw new Exception("directory creation failed : " + configDir.getAbsolutePath());
		}
		File mimeTypes = new File(configDir, "mime.types");
		if (!mimeTypes.isFile()) {
			IO.copy(getClass().getResource("/data/mime.types"), mimeTypes);
		}
		File nginxConf = new File(configDir, "nginx.conf");
		IO.copy(new StringReader(generateConfigFile(config)), new FileWriter(nginxConf));
		File logsDir = new File(data, "logs");
		if (!logsDir.isDirectory()) {
			boolean created = logsDir.mkdir();
			if (!created)
				throw new Exception("directory creation failed : " + logsDir.getAbsolutePath());
		}
		StringBuilder sb = new StringBuilder();
		sb.append(nginx.getAbsolutePath()).append(" -p " + data.getAbsolutePath());
		sb.append(" -c " + nginxConf.getAbsolutePath());
		sb.append(" -g \"daemon off;\"");
		pd.startScript = sb.toString();
		sb = new StringBuilder();
		sb.append(nginx.getAbsolutePath()).append(" -s stop");
		pd.stopScript = sb.toString();
		sb = new StringBuilder();
		sb.append("ps x | grep nginx | grep 'master process'");
		pd.statusScript = sb.toString();
		pd.description = "Nginx Packager";
		return pd;
	}
	
	private String generateConfigFile(NginxProperties properties) {
		StringBuilder sb = new StringBuilder();
		if (properties.username() != null) {
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
