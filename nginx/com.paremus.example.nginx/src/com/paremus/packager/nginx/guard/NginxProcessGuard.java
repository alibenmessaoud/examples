package com.paremus.packager.nginx.guard;

import java.util.Map;

import org.bndtools.service.packager.ProcessGuard;
import com.paremus.service.nginx.NginxProperties;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;

@Component(designateFactory = NginxProperties.class, properties = { "type=nginx" }, name="com.paremus.example.nginx.guard")
public class NginxProcessGuard implements ProcessGuard {
	
	private Map<String, Object> props;
	
	@Activate
	void activate(Map<String, Object> props) {
		this.props = props;
	}

	@Override
	public Map<String, Object> getProperties() {
		return props;
	}

	@Override
	public synchronized void state(State state) throws Exception {
		if (state.isAlive()) {
			if (state == State.STARTED)
				System.out.println("Nginx App STARTED");
		} else {
			System.out.println("Nginx App is down");
		}
	}

}
