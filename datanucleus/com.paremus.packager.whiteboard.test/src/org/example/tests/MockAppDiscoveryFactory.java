package org.example.tests;

import java.util.HashMap;
import java.util.Map;

import com.paremus.packager.api.Scope;
import com.paremus.packager.api.discovery.ApplicationDiscoveryService;
import com.paremus.packager.api.discovery.ApplicationDiscoveryServiceFactory;

public class MockAppDiscoveryFactory implements ApplicationDiscoveryServiceFactory {

    private final Map<String,MockAppDiscoveryService> serviceMap = new HashMap<String,MockAppDiscoveryService>();

    public MockAppDiscoveryService getServiceForScope(Scope scope) {
        MockAppDiscoveryService service = serviceMap.get(scope.getScopeKey());
        if (service == null) {
            service = new MockAppDiscoveryService(scope);
            serviceMap.put(scope.getScopeKey(), service);
        }
        return service;
    }

}
