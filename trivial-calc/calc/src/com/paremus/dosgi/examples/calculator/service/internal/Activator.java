
package com.paremus.dosgi.examples.calculator.service.internal;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.remoteserviceadmin.RemoteConstants;

import com.paremus.dosgi.examples.calculator.api.CalculatorService;
import com.paremus.dosgi.examples.calculator.service.MyCalculatorService;

/**
 * Extension of the default OSGi bundle activator
 */
public final class Activator implements BundleActivator {

    private MyCalculatorService service;

    /**
     * Called whenever the OSGi framework starts our bundle
     */
    public synchronized void start(BundleContext bc) throws Exception {

        // create our service
        service = new MyCalculatorService();

        // service regsitration properties
        Dictionary<String, Object> properties = new Hashtable<String, Object>();

        // required: the name of our exposed service interface
        properties.put(RemoteConstants.SERVICE_EXPORTED_INTERFACES, CalculatorService.class.getName());

        // required: the name of the configuration type used for export
        properties.put(RemoteConstants.SERVICE_EXPORTED_CONFIGS, "com.paremus.dosgi.essencermi");

        // optional: a public-facing host name or IP address
        // the essence-rmi distribution provider will try to figure out "the"
        // public IP address, but this is often impossible to do correctly without
        // external help.
        // properties.put("com.paremus.dosgi.essencermi.host", "yourhost");

        // optional: bind to a specific port
        // properties.put("com.paremus.dosgi.essencermi.port", "9000");

        bc.registerService(CalculatorService.class.getName(), service, properties);
    }

    /**
     * Called whenever the OSGi framework stops our bundle
     */
    public synchronized void stop(BundleContext bc) throws Exception {
        service.shutdown();
        service = null;
    }

}
