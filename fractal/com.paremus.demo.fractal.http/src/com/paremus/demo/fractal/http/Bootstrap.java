package com.paremus.demo.fractal.http;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

import javax.servlet.ServletException;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.osgi.service.async.Async;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;

import com.paremus.demo.fractal.api.ColourMap;
import com.paremus.demo.fractal.api.Equation;

/**
 * This component sets up the UI and REST services
 */
@Component
public class Bootstrap {
	/**
	 * The context root for the application
	 */
	public static final String ROOT_APP_PATH = "/paremus/demo/fractal";

	/** The web endpoint to register with */
	private HttpService httpService;

	/** The async service to use when making asynchronous calls */
	private Async asyncService;
	
	/** The Equation Services */
	private final ConcurrentMap<String, Equation> equations = 
			new ConcurrentHashMap<String, Equation>();
	
	/** Available Equations, and their default configurations. Sort by name for UI consistency */
	private final ConcurrentMap<String, Map<String, Object>> equationConfig = 
			new ConcurrentSkipListMap<String, Map<String, Object>>();

	/** Available Colour Schemes. Sort by name for UI consistency */
	private final ConcurrentMap<String, ColourMap> colourSchemes = 
			new ConcurrentSkipListMap<String, ColourMap>();

	/** The Jersey REST container */
	private volatile ServletContainer container;
	
	
	@Reference(multiple=true, dynamic=true)
	void addEquation(Equation equation, Map<String, Object> props) {
		equations.putIfAbsent(String.valueOf(props.get(Equation.EQUATION_TYPE)), equation);
		equationConfig.putIfAbsent(String.valueOf(props.get(Equation.EQUATION_TYPE)), new HashMap<String, Object>(props));
	}

	void removeEquation(Equation equation, Map<String, Object> props) {
		equationConfig.remove(String.valueOf(props.get(Equation.EQUATION_TYPE)), equation);
	}

	@Reference(multiple=true, dynamic=true, service=ColourMap.class)
	void addColourMap(ColourMap colourMap, Map<String, Object> props) {
		colourSchemes.putIfAbsent(String.valueOf(props.get(ColourMap.PROFILE_NAME)), colourMap);
	}

	void removeColourMap(ColourMap colourMap, Map<String, Object> props) {
		colourSchemes.remove(String.valueOf(props.get(ColourMap.PROFILE_NAME)), colourMap);
	}

	@Reference
	void setHttpService(HttpService httpService) {
		this.httpService = httpService;
	}

	@Reference
	void setAsyncService(Async asyncService) {
		this.asyncService = asyncService;
	}

	@Activate
	void start() throws ServletException, NamespaceException {
		// Set up the REST services
		ResourceConfig config = new ResourceConfig();
		config.registerInstances(
				new FormDataProvider(equationConfig.values(), colourSchemes.keySet()),
				new Renderer(equations, colourSchemes, asyncService)
			);
		
		container = new ServletContainer(config);

		// Register the rest services and the static content
		httpService.registerServlet(ROOT_APP_PATH + "/rest",  container, null, null);
		httpService.registerResources(ROOT_APP_PATH, "static", new RedirectingStaticContentHandler());
		
	}

	@Deactivate
	void destroy() throws ServletException, NamespaceException {
		try {
			httpService.unregister(ROOT_APP_PATH + "/rest");
			httpService.unregister(ROOT_APP_PATH);
		} finally {
			container.destroy();
		}
		
	}
}