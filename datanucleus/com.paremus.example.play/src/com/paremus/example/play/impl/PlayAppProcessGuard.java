package com.paremus.example.play.impl;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;

import com.paremus.example.play.PlayAppProperties;
import com.paremus.service.endpoint.Endpoint;
import com.paremus.service.packager.ProcessGuard;

@Component(
		name = "com.paremus.example.play.guard",
		designateFactory = PlayAppProperties.class,
		properties = "type=play-rest"
		)
public class PlayAppProcessGuard implements ProcessGuard {
	
	private Map<String, String> config;
	private String restUrl;

	@Activate
	void activate(Map<String, String> config) {
		this.config = config;
	}

	@Reference(target = "(uri=midtier:*)")
	void bindRestURL(Endpoint endpoint, Map<String, String> props) throws Exception {
		URI midtierUri = new URI(props.get(Endpoint.URI));
		restUrl = midtierUri.getSchemeSpecificPart();
	}
	
	public Map<String, Object> getProperties() {
		HashMap<String, Object> props = new HashMap<String, Object>();
		props.putAll(config);
		props.put("restUrl", restUrl);
		return props;
	}

	public void state(State state) throws Exception {
		if (state.isAlive())
			System.out.println("Play REST App is running");
		else
			System.out.println("Play REST App is down");
	}

}
