package com.paremus.example.restclient;

import java.io.IOException;
import java.io.PrintStream;

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
				"osgi.command.scope=repo",
				"osgi.command.function=list"
		})
public class RepositoriesCommands {
	
	private static final String BASE_URL = "http://localhost:9000/fabric/repos";
	
	@Descriptor("List fabric repositories")
	public void list() throws IOException {
		PrintStream out = System.out;
		
		// Fetch the JSON
		ClientResource resource = new ClientResource(BASE_URL);
		Representation representation = resource.get(MediaType.APPLICATION_JSON);
		
		// Parse JSON into a tree.
		// Not the fastest approach, but much more convenient than the streaming API.
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.readTree(representation.getStream());
		out.printf("Found %d repositories:\n", rootNode.size());
		
		// Iterate over array entries
		for(JsonNode node : rootNode) {
			String name = node.get("name").asText();
			String address = node.get("address").asText();
			out.printf("%s ==> %s\n", name, address);
		}
	}
	
}
