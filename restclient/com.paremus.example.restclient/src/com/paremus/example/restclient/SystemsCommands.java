package com.paremus.example.restclient;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.net.URLEncoder;

import org.apache.felix.service.command.Descriptor;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import aQute.bnd.annotation.component.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component(
		provide = Object.class,
		properties = {
				"osgi.command.scope=system",
				"osgi.command.function=list|show|install|uninstall"
		})
public class SystemsCommands {
	
	private static final String BASE_URL = "http://localhost:9000/fabric/systems";
	
	private final PrintStream out = System.out;
	
	@Descriptor("List systems")
	public void list() throws IOException {
		// Fetch the JSON
		ClientResource resource = new ClientResource(BASE_URL);
		Representation representation = resource.get(MediaType.APPLICATION_JSON);
		
		// Parse JSON into a tree.
		// Not the fastest approach, but much more convenient than the streaming API.
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.readTree(representation.getStream());
		out.printf("Found %d systems:\n", rootNode.size());
		
		// Iterate over array entries
		for(JsonNode node : rootNode) {
			String id = node.get("id").asText();
			String systemUri = node.get("systemUri").asText();
			boolean deployed = node.get("deployed").asBoolean();
			boolean valid = node.get("valid").asBoolean();
			
			out.printf("%s ==> %s (deployed=%b, valid=%b)\n", id, systemUri, deployed, valid);
		}
	}
	
	@Descriptor("Show system detail")
	public void show(@Descriptor("System URI of selected system") String systemUri) throws IOException {
		out.printf("Showing details for system: %s\n", systemUri);
		ClientResource resource = new ClientResource(BASE_URL + "/" + URLEncoder.encode(systemUri, "UTF-8"));
		Representation representation = resource.get();
		
		StringWriter outputBuffer = new StringWriter();
		representation.write(outputBuffer);
		
		out.println(outputBuffer.toString());
	}
	
	/*
	 * Install a new system doc with the HTTP POST method. The URL of the new resource is returned
	 * in the Location header.
	 */
	@Descriptor("Install system document")
	public void install(@Descriptor("Path to system document on local filesystem") String path) throws IOException {
		File file = new File(path);
		if (!file.isFile())
			throw new IllegalArgumentException("System document does not exist: " + path);
		
		ClientResource resource = new ClientResource(BASE_URL);
		resource.post(file);
		
		// The new system URL is in the Location header of the response
		Response response = resource.getResponse();
		String location = response.getLocationRef().toString();
		out.printf("System uploaded to location %s\n", location);
	}
	
	/*
	 * Uninstall a system doc with the HTTP DELETE method. Note that according to the HTTP specification,
	 * DELETE is supposed to be idempotent, i.e. can be repeated with the same parameters without causing
	 * errors. Therefore uninstalling a system URI that does not exist will have no effect.
	 */
	@Descriptor("Uninstall system")
	public void uninstall(@Descriptor("System URI to uninstall") String systemUri) throws IOException {
		out.printf("Uninstalling system with URI %s\n", systemUri);
		
		ClientResource resource = new ClientResource(BASE_URL + "/" + URLEncoder.encode(systemUri, "UTF-8"));
		resource.delete();
	}
	
}
