package com.paremus.example.datanucleus.connector.derby;

import java.util.HashMap;
import java.util.Map;

import javax.jdo.PersistenceManagerFactory;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;

import com.paremus.datanucleus.api.PersistenceManagerFactoryBuilder;
import com.paremus.service.endpoint.Endpoint;

@Component
public class DerbyConnector {
    
    public static final String APP_URI_PREFIX = "jdbc:derby";
    public static final String APP_URI_FILTER = "(uri=" + APP_URI_PREFIX + ":*)";
    
    public static final String DATABASE_NAME = "DataNucleusExample;create=true";
    
    private String jdbcUrl;
    private PersistenceManagerFactoryBuilder builder;
    
    private PersistenceManagerFactory persistenceMgrFactory;
    public ServiceRegistration registration;
    
    @Reference(target = APP_URI_FILTER)
    public void bindPublishedApp(Endpoint endpoint, Map<String, String> props) {
        this.jdbcUrl = props.get(Endpoint.URI) + "/" + DATABASE_NAME;
    }
    
    @Reference
    public void bindModel(PersistenceManagerFactoryBuilder builder) {
        this.builder = builder;
    }
    
    @Activate
    public void activate(BundleContext context) throws Exception {
        Map<String,Object> pmfProperties = new HashMap<String, Object>();
        
        pmfProperties.put("javax.jdo.option.ConnectionURL", jdbcUrl);
        pmfProperties.put("javax.jdo.option.ConnectionDriverName", "org.apache.derby.jdbc.ClientDriver");
        pmfProperties.put("javax.jdo.option.ConnectionUserName", "APP");
        pmfProperties.put("javax.jdo.option.ConnectionPassword", "APP");
        
        persistenceMgrFactory = builder.createPersistenceManagerFactory(pmfProperties);
        registration = context.registerService(PersistenceManagerFactory.class.getName(), persistenceMgrFactory, null);
    }
    
    @Deactivate
    public void deactivate() {
        registration.unregister();
        persistenceMgrFactory.close();
    }

}
