package com.paremus.example.play.impl;

import java.util.HashMap;
import java.util.Map;

import org.bndtools.service.endpoint.Endpoint;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;

import com.paremus.example.play.PlayAppProperties;
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

	@Reference(target = "(uri=http://*/blog)")
	void bindRestURL(Endpoint endpoint, Map<String, String> props) throws Exception {
		restUrl = props.get(Endpoint.URI);
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
