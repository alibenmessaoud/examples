package com.paremus.example.datanucleus.service.rest;

import java.net.URI;

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

@Component
public class Publisher {
    
    private final int DEFAULT_HTTP_PORT = 8080;
    
    private ApplicationDiscoveryServiceFactory applicationDiscoveryServiceFactory;
    private ApplicationDiscoveryService applicationDiscoveryService;
    private ApplicationServiceReference appRef;
    
    @Reference
    public void setApplicationDiscoveryServiceFactory(ApplicationDiscoveryServiceFactory applicationDiscoveryServiceFactory) {
        this.applicationDiscoveryServiceFactory = applicationDiscoveryServiceFactory;
    }
    
    @Activate
    public void activate(BundleContext context) throws Exception {
        applicationDiscoveryService = applicationDiscoveryServiceFactory.getServiceForScope(new Scope("global"));

        String httpHostName = applicationDiscoveryService.getHostname();
        int httpPort = findHttpPort(context);
        URI httpUri = new URI("http", null, httpHostName, httpPort, "/blog/comments", null, null);
        URI midtierUri = new URI("midtier", httpUri.toString(), null);
        System.out.println("====> Publishing URI " + midtierUri);
        appRef = new ApplicationServiceReference(midtierUri.toString());
        
        applicationDiscoveryService.publish(appRef);
    }
    
    @Deactivate
    public void deactivate() {
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
