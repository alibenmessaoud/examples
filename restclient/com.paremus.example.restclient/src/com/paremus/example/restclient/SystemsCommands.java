package com.paremus.example.restclient;

import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.net.URLEncoder;

import org.apache.felix.service.command.Descriptor;
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
				"osgi.command.function=list|show"
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
		Representation representation = resource.get(MediaType.TEXT_PLAIN);
		
		StringWriter outputBuffer = new StringWriter();
		representation.write(outputBuffer);
		
		out.println(outputBuffer.toString());
	}
	
}
