package com.paremus.examples.mqtt.mockdata;

import java.net.URI;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.bndtools.service.endpoint.Endpoint;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;

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
		URI boundUri = URI.create(endpointUri);
		URI tcpUri = new URI("tcp", null, boundUri.getHost(), boundUri.getPort(), null, null, null);

		final MqttClient mqttClient = new MqttClient(tcpUri.toString(), clientId);
		mqttClient.connect();
		
		System.out.println("[mockdata]: starting random dose generation");
		random = new Random();
		executor = Executors.newScheduledThreadPool(1);
		executor.scheduleAtFixedRate(new Runnable() {
			public void run() {
				double randomDose = random.nextDouble() * 100d;
				System.out.printf("[mockdata]: Sending random dose value of %f%n", randomDose);
				try {
					mqttClient.getTopic("geiger").publish(Double.toString(randomDose).getBytes(), 0, false);
				} catch (MqttException e) {
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
