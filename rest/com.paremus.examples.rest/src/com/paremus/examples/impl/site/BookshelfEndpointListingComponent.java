package com.paremus.examples.impl.site;

import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.bndtools.service.endpoint.Endpoint;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

/**
 * This component creates a REST endpoint that lists Bookshelf endpoint URLs, as
 * advertised within OSGi by the {@link Endpoint} service type. In other words a
 * REST client can get a list of available RESTful Bookshelf services, as JSON
 * object array.
 */
@Component(immediate = true, provide = Object.class, properties = {
	"osgi.rest.alias=/endpoints"
})
@Path("/")
public class BookshelfEndpointListingComponent {
	
	private final List<String> endpointUris = new LinkedList<String>();
	
	@Reference(type = '*', target = "(uri=http://*/bookshelf)")
	synchronized void bindEndpoint(Endpoint endpoint, Map<String, String> props) {
		endpointUris.add(props.get(Endpoint.URI));
	}
	synchronized void unbindEndpoint(Endpoint endpoint, Map<String, String> props) {
		endpointUris.remove(props.get(Endpoint.URI));
	}

	@GET
	public synchronized String listEndpoints() throws Exception {
		StringWriter writer = new StringWriter();
		JsonGenerator generator = new JsonFactory().createJsonGenerator(writer);
		generator.writeStartArray();
		for (String uri : endpointUris) {
			generator.writeStartObject();
			generator.writeStringField("uri", uri);
			generator.writeEndObject();
		}
		generator.writeEndArray();
		generator.close();
		return writer.toString();
	}
	
}
