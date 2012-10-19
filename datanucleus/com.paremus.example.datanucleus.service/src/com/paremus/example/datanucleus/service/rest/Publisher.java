package com.paremus.example.datanucleus.service.rest;

import java.net.InetAddress;
import java.net.URI;
import java.util.Properties;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;

import com.paremus.service.endpoint.Endpoint;

@Component
public class Publisher implements Endpoint {

    private final int DEFAULT_HTTP_PORT = 8080;
    
    private ServiceRegistration endpointReg;

    @Activate
    public synchronized void activate(BundleContext context) throws Exception {
        String localhost = InetAddress.getLocalHost().getHostAddress();
        int httpPort = findHttpPort(context);
        
        URI httpUri = new URI("http", null, localhost, httpPort, "/blog", null, null);
        URI midtierUri = new URI("midtier", httpUri.toString(), null);
        
        Properties props = new Properties();
        props.put(Endpoint.URI, midtierUri.toString());
        endpointReg = context.registerService(Endpoint.class.getName(), new Endpoint() {}, props);
    }
    
    @Deactivate
    public synchronized void deactivate() {
        endpointReg.unregister();
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
