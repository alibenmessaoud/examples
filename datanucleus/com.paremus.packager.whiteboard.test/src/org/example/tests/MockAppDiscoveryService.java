package org.example.tests;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.paremus.packager.api.PackagerException;
import com.paremus.packager.api.Scope;
import com.paremus.packager.api.discovery.ApplicationDiscoveryEvent;
import com.paremus.packager.api.discovery.ApplicationDiscoveryListener;
import com.paremus.packager.api.discovery.ApplicationDiscoveryService;
import com.paremus.packager.api.discovery.ApplicationServiceReference;

public class MockAppDiscoveryService implements ApplicationDiscoveryService {

    private final List<ApplicationDiscoveryListener> listeners = new LinkedList<ApplicationDiscoveryListener>();
    private final Scope scope;

    public MockAppDiscoveryService(Scope scope) {
        this.scope = scope;
    }
    
    void fireServiceReferenceChange(Set<ApplicationServiceReference> refs) throws Exception {
        ApplicationDiscoveryEvent event = new ApplicationDiscoveryEvent(refs);
        
        for (ApplicationDiscoveryListener listener : listeners) {
            listener.serviceReferenceChange(event);
        }
    }

    public void addListener(ApplicationDiscoveryListener l) throws PackagerException {
        listeners.add(l);
    }

    public Set<ApplicationServiceReference> getActiveReferences() {
        return null;
    }

    @Override
    public void publish(ApplicationServiceReference ref) throws PackagerException {}

    @Override
    public void removeListener(ApplicationDiscoveryListener l) {
        listeners.remove(l);
    }

    @Override
    public void retract(ApplicationServiceReference ref) {}

    @Override
    public void shutdown() {}

}
