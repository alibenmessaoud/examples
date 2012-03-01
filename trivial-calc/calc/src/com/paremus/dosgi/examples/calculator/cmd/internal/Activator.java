
package com.paremus.dosgi.examples.calculator.cmd.internal;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.service.remoteserviceadmin.RemoteConstants;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.paremus.dosgi.examples.calculator.api.CalculatorService;
import com.paremus.dosgi.examples.calculator.cmd.CalculatorCommand;

/**
 * Extension of the default OSGi bundle activator
 */
public final class Activator implements BundleActivator, ServiceTrackerCustomizer {

    private static final String COMMAND_SCOPE = "osgi.command.scope";
    private static final String COMMAND_FUNCTION = "osgi.command.function";

    private BundleContext bundleContext;
    private CalculatorCommand command;
    private ServiceTracker serviceTracker;

    /**
     * Called whenever the OSGi framework starts our bundle
     */
    public synchronized void start(BundleContext bc) throws Exception {
        bundleContext = bc;

        // register the command processor
        command = new CalculatorCommand();
        Hashtable<String, Object> properties = new Hashtable<String, Object>();
        properties.put(COMMAND_SCOPE, "calc");
        properties.put(COMMAND_FUNCTION, new String[]{"help", "status", "add", "benchmark"});
        bc.registerService(CalculatorCommand.class.getName(), command, properties);

        // we only want "remote" CalculatorService references
        String objClass = "(" + Constants.OBJECTCLASS + "=" + CalculatorService.class.getName() + ")";
        String remote = "(" + RemoteConstants.SERVICE_IMPORTED + "=" + "*" + ")";
        Filter sf = bc.createFilter("(&" + objClass + remote + ")");
        serviceTracker = new ServiceTracker(bundleContext, sf, this);
        serviceTracker.open();
    }

    /**
     * Called whenever the OSGi framework stops our bundle
     */
    public synchronized void stop(BundleContext bc) throws Exception {
        command.shutdown();
        serviceTracker.close();
    }

    /**
     * Called whenever a tracked service was found
     */
    public synchronized Object addingService(ServiceReference reference) {
        CalculatorService service = (CalculatorService)bundleContext.getService(reference);
        command.setCalculatorService(service);
        return service;
    }

    /**
     * Called whenever a tracked service was modified
     */
    public synchronized void modifiedService(ServiceReference reference, Object service) {
        // ignore (for now)
    }

    /**
     * Called whenever a tracked service was removed
     */
    public synchronized void removedService(ServiceReference reference, Object service) {
        bundleContext.ungetService(reference);
        command.setCalculatorService(null);
    }

}
