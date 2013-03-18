package com.paremus.service.nginx;

import aQute.bnd.annotation.metatype.Meta;

/**
 * <pre>
 * nginx version: nginx/1.2.7
 * Usage: nginx [-?hvVtq] [-s signal] [-c filename] [-p prefix] [-g directives]
 *
 * Options:
 *   -?,-h         : this help
 *   -v            : show version and exit
 *   -V            : show version and configure options then exit
 *   -t            : test configuration and exit
 *   -q            : suppress non-error messages during configuration testing
 *   -s signal     : send signal to a master process: stop, quit, reopen, reload
 *   -p prefix     : set prefix path (default: /usr/local/Cellar/nginx/1.2.7/)
 *   -c filename   : set configuration file (default: /usr/local/etc/nginx/nginx.conf)
 *   -g directives : set global directives out of configuration file
 *</pre>
 */
public interface NginxProperties {
	
	String username();
	String groupname();
	@Meta.AD(required = false, deflt = "1")
	int workerProcesses();
	int workerPriority();
	@Meta.AD(required = false, deflt = "1024")
	int workerConnections();
	@Meta.AD(required = false, deflt = "8080")
	int listen();
	@Meta.AD(required = false, deflt = "localhost")
	String serverName();
	@Meta.AD(required = false, deflt = "html")
	String root();
	@Meta.AD(required = false, deflt= "http://localhost:8080")
	String proxyPass();
	
}
