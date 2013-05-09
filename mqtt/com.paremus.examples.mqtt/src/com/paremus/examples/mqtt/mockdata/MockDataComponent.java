package com.paremus.examples.mqtt.mockdata;

import java.net.InetAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.bndtools.service.endpoint.Endpoint;
import org.eclipse.paho.client.mqttv3.MqttClient;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;

import com.paremus.examples.util.Marshaller;

/**
 * Generates random geiger-counter data and sends it to the MQTT server using
 * topic "geiger".
 * 
 * @author Neil Bartlett <neil.bartlett@paremus.com>
 * 
 */
@Component
public class MockDataComponent {

	private final String clientId = Long.toString(System.currentTimeMillis());

	private String endpointUri;
	
	private ScheduledExecutorService executor;
	private Random random;
	
	@Reference
	public void setEndpoint(Endpoint endpoint, Map<String, String> endpointProps) {
		endpointUri = endpointProps.get(Endpoint.URI);
		System.out.println("[mockdata]: bound to MQTT server URI: " + endpointUri);
	}
	
	@Activate
	public void start() throws Exception {
		// Connect to MQTT
		URI boundUri = URI.create(endpointUri);
		URI tcpUri = new URI("tcp", null, boundUri.getHost(), boundUri.getPort(), null, null, null);

		final MqttClient mqttClient = new MqttClient(tcpUri.toString(), clientId);
		mqttClient.connect();
		
		// Get host name
		final String hostName = InetAddress.getLocalHost().getHostName();
		
		System.out.println("[mockdata]: starting random dose generation");
		random = new Random();
		executor = Executors.newScheduledThreadPool(1);
		executor.scheduleAtFixedRate(new Runnable() {
			public void run() {
				try {
					HashMap<String, Object> map = new HashMap<String, Object>();
					map.put("dose", random.nextDouble() * 100d);
					map.put("time", System.currentTimeMillis());
					map.put("source", hostName + " (MOCK DATA)");
					mqttClient.getTopic("geiger").publish(Marshaller.marshal(map), 0, false);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, 2, 2, TimeUnit.SECONDS);
	}
	
	@Deactivate
	public void stop() {
		executor.shutdown();
		System.out.println("[mockdata]: stopped random dose generation");
	}
}
