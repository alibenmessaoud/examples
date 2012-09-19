package org.example.tests;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import com.paremus.packager.api.Scope;
import com.paremus.packager.api.discovery.ApplicationDiscoveryServiceFactory;
import com.paremus.packager.api.discovery.ApplicationServiceReference;
import com.paremus.packager.whiteboard.api.PublishedApplication;

public class ExampleTest extends TestCase {

    private final static Scope SCOPE_TEST = new Scope("test");

    private final BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();

    public void testExample() throws Exception {
        MockAppDiscoveryFactory mockFactory = new MockAppDiscoveryFactory();
        context.registerService(ApplicationDiscoveryServiceFactory.class.getName(), mockFactory, null);

        Set<ApplicationServiceReference> refs;
        ServiceReference[] serviceRefs;

        // Publish a single application URI -> should see one PublishedApp service
        refs = new HashSet<ApplicationServiceReference>();
        refs.add(new ApplicationServiceReference("mongodb:/1.2.3.4:5678"));
        mockFactory.getServiceForScope(SCOPE_TEST).fireServiceReferenceChange(refs);

        serviceRefs = context.getAllServiceReferences(PublishedApplication.class.getName(), null);
        assertNotNull(serviceRefs);
        assertEquals(1, serviceRefs.length);
        assertEquals("mongodb:/1.2.3.4:5678", serviceRefs[0].getProperty("uri"));

        // Add another URI -> should see to services
        refs.add(new ApplicationServiceReference("derby:/localhost"));
        mockFactory.getServiceForScope(SCOPE_TEST).fireServiceReferenceChange(refs);

        serviceRefs = context.getAllServiceReferences(PublishedApplication.class.getName(), null);
        assertNotNull(serviceRefs);
        assertEquals(2, serviceRefs.length);
        
        // Drop back to a single URI -> back to one service
        refs = new HashSet<ApplicationServiceReference>();
        refs.add(new ApplicationServiceReference("derby:/localhost"));
        mockFactory.getServiceForScope(SCOPE_TEST).fireServiceReferenceChange(refs);
        
        serviceRefs = context.getAllServiceReferences(PublishedApplication.class.getName(), null);
        assertNotNull(serviceRefs);
        assertEquals(1, serviceRefs.length);
        assertEquals("derby:/localhost", serviceRefs[0].getProperty("uri"));
        
        // Drop down to zero URIs -> no services
        refs.clear();
        mockFactory.getServiceForScope(SCOPE_TEST).fireServiceReferenceChange(refs);
        
        serviceRefs = context.getAllServiceReferences(PublishedApplication.class.getName(), null);
        assertNull(serviceRefs);
    }
}
