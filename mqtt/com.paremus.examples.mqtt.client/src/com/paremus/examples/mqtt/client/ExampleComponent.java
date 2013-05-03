package com.paremus.examples.mqtt.client;

import java.util.Map;

import org.bndtools.service.endpoint.Endpoint;
import org.osgi.service.remoteserviceadmin.RemoteConstants;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;

@Component(immediate = true)
public class ExampleComponent {

	@Reference(type = '*')
	public void addEndpoint(Endpoint endpoint, Map<String, String> endpointProps) {
		String uri = endpointProps.get(Endpoint.URI);
		boolean imported = endpointProps.get(RemoteConstants.SERVICE_IMPORTED) != null;
		System.out.printf("====> ADDED endpoint URI=%s, imported=%b%n", uri, imported);
	}
	public void removeEndpoint(Endpoint endpoint, Map<String, String> endpointProps) {
		String uri = endpointProps.get(Endpoint.URI);
		boolean imported = endpointProps.get(RemoteConstants.SERVICE_IMPORTED) != null;
		System.out.printf("====> REMOVED endpoint URI=%s, imported=%b%n", uri, imported);
	}
	
}