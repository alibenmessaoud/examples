package com.paremus.example.datanucleus.service.rest;

import java.net.URI;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;

import com.paremus.packager.api.Scope;
import com.paremus.packager.api.discovery.ApplicationDiscoveryService;
import com.paremus.packager.api.discovery.ApplicationDiscoveryServiceFactory;
import com.paremus.packager.api.discovery.ApplicationServiceReference;
import com.paremus.packager.whiteboard.api.PublishedApplication;

@Component
public class Publisher {

    private final int DEFAULT_HTTP_PORT = 8080;

    private ApplicationDiscoveryService applicationDiscoveryService;
    private ApplicationServiceReference appRef;

    private String scope;

    @Reference(target = "(|(uri=mongodb:*)(uri=jdbc:derby:*))")
    public void setPublishedApplication(PublishedApplication app, Map<String, Object> svcProps) {
        System.out.println("===> Blog application: bound to Database app service on URI " + svcProps.get(PublishedApplication.PROP_URI));
    }
    public void unsetPublishedApplication(PublishedApplication app) {
        System.out.println("===> Blog application: UNbound from database app service");
    }
    
    @Reference(target = "(scope=global)")
    public void setApplicationDiscoveryService(ApplicationDiscoveryService applicationDiscoverService, Map<String, Object> svcProps) {
        this.scope = (String) svcProps.get("scope");
        this.applicationDiscoveryService = applicationDiscoverService;
        System.out.println("===> Blog application: BOUND application discovery service with scope " + scope);
    }
    public void unsetApplicationDiscoveryService(ApplicationDiscoveryService applicationDiscoveryService) {
        System.out.println("===> Blog application: UNBOUND application discovery service with scope " + scope);
    }
    
    @Activate
    public void activate(BundleContext context) throws Exception {
        String httpHostName = applicationDiscoveryService.getHostname();
        int httpPort = findHttpPort(context);
        URI httpUri = new URI("http", null, httpHostName, httpPort, "/blog", null, null);
        URI midtierUri = new URI("midtier", httpUri.toString(), null);
        
        int ttl = 100000;
        System.out.printf("====> Publishing discovery URI=%s, TTL=%d, scope=%s.%n", midtierUri, ttl, scope);
        appRef = new ApplicationServiceReference(midtierUri.toString(), ttl);
        
        applicationDiscoveryService.publish(appRef);
    }
    
    @Deactivate
    public void deactivate() {
        System.out.printf("====> Retracting discovery URI=%s, scope=%s.%n", appRef.getUri(), scope);
        applicationDiscoveryService.retract(appRef);
    }

    private int findHttpPort(BundleContext context) throws Exception {
        ServiceReference[] refs = context.getAllServiceReferences(null, "(jetty.port=*)");
        if (refs == null || refs.length == 0) {
            System.err.println("====> WARNING: Unable to find Jetty port, defaulting to " + DEFAULT_HTTP_PORT);
            return DEFAULT_HTTP_PORT;
        }
        
        if (refs.length > 2)
            System.err.println("====> WARNING: More than one Jetty port appears to be opening, using first");
        
        int portNum;
        Object portObj = refs[0].getProperty("jetty.port");
        if (portObj instanceof Number)
            portNum = ((Number) portObj).intValue();
        else if (portObj instanceof String)
            portNum = Integer.parseInt((String) portObj);
        else
            throw new Exception(String.format("Invalid type for property jetty.port on service ID %d. Expected number or String, was %s.", refs[0].getProperty(Constants.SERVICE_ID), portObj.getClass().getName()));
        
        System.out.println("====> Returning jetty.port=" + portNum);
        return portNum;
    }
    
}
