package com.paremus.packager.nginx.guard;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.bndtools.service.packager.ProcessGuard;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.metatype.Configurable;

import com.paremus.service.nginx.NginxProperties;

@Component(designateFactory = NginxProperties.class, 
	properties = {ProcessGuard.PACKAGE_TYPE + "=nginx", ProcessGuard.VERSION + "=1.2.8"}, 
	name="com.paremus.example.nginx.guard")
public class NginxProcessGuard implements ProcessGuard {
	
	private Map<String, Object> props;
	
	@Activate
	void activate(Map<String, Object> props) throws Exception {
		NginxProperties config = Configurable.createConfigurable(NginxProperties.class, props);
		
		this.props = new HashMap<String, Object>();
		this.props.putAll(props);

		// apply defaults back into the props (bit of a hack, can we improve this?)
		for (Method method : NginxProperties.class.getDeclaredMethods()) {
			String name = method.getName();
			Object value = method.invoke(config);
			this.props.put(name, value);
		}
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
