package com.paremus.datanucleus.extender.test;

import java.util.Properties;

import javax.jdo.PersistenceManagerFactory;

import junit.framework.TestCase;

import org.bndtools.service.endpoint.Endpoint;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

import com.paremus.datanucleus.api.DatabaseConnectionConfigurer;
import com.paremus.datanucleus.api.PersistenceManagerFactoryBuilder;

public class ExampleTest extends TestCase {

	private final BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testExample() throws Exception {
		// Publish a DN model
		MockPMFBuilder mockPMFB = new MockPMFBuilder();
		Properties mockPMFBProps = new Properties();
		mockPMFBProps.put(PersistenceManagerFactoryBuilder.PROP_MODEL_NAME, "DummyModel");
		ServiceRegistration mockPMFBReg = context.registerService(PersistenceManagerFactoryBuilder.class.getName(), mockPMFB, mockPMFBProps);
		
		// Publish an application marker service (as provided by packager with the whiteboard bundle)
		Endpoint appMarker = new Endpoint() {};
		Properties appMarkerProps = new Properties();
		appMarkerProps.put(Endpoint.URI, "funkyDb://1.1.2.2:8080");
		ServiceRegistration appMarkerReg = context.registerService(Endpoint.class.getName(), appMarker, appMarkerProps);
		
		// Publish a datasource configurer
		MockDatabaseConfigurer configurer = new MockDatabaseConfigurer();
		Properties configurerProps = new Properties();
		configurerProps.put("uriMatch", "funkyDb:*");
		ServiceRegistration configurerReg = context.registerService(DatabaseConnectionConfigurer.class.getName(), configurer, configurerProps);
		
		// Expect that the DB model gets called with a JDBC URL that comes from the configurer
		assertEquals("jdbc:dummy:1.1.2.2:8080/DummyDB", mockPMFB.getProps().get("javax.jdo.option.ConnectionURL"));
		
		// Check for a PMF service with model = DummyModel
		ServiceReference[] pmfSvcRefs = context.getAllServiceReferences(PersistenceManagerFactory.class.getName(), null);
		assertNotNull(pmfSvcRefs);
		assertEquals(1, pmfSvcRefs.length);
		assertEquals("DummyModel", pmfSvcRefs[0].getProperty(PersistenceManagerFactoryBuilder.PROP_MODEL_NAME));
	}
}
