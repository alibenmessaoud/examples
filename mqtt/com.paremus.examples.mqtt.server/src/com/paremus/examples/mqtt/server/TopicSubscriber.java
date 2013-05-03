package com.paremus.examples.mqtt.server;

import java.net.URI;
import java.util.Map;

import org.bndtools.service.endpoint.Endpoint;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;

@Component
public class TopicSubscriber {

	private final String clientId = Long.toString(System.currentTimeMillis());
	
	private String mqttUri;
	private MqttClient client;

	@Reference(target = "(uri=mqtt://*)")
	public void setEndpoint(Endpoint endpoint, Map<String, String> endpointProps) {
		mqttUri = endpointProps.get(Endpoint.URI);
	}
	
	@Activate
	public void start() throws Exception {
		URI boundUri = URI.create(mqttUri);
		URI tcpUri = new URI("tcp", null, boundUri.getHost(), boundUri.getPort(), null, null, null);
		
		MqttCallback callback = new MqttCallback() {
			@Override
			public void messageArrived(MqttTopic topic, MqttMessage message) throws Exception {
				System.out.printf("RECEIVED, topic=%s, message=%s%n", topic.getName(), new String(message.getPayload()));
			}
			@Override
			public void deliveryComplete(MqttDeliveryToken token) {
			}
			@Override
			public void connectionLost(Throwable ex) {
				System.err.println("Lost connection to MQTT server");
				if (ex != null)
					ex.printStackTrace();
			}
		};
		client = new MqttClient(tcpUri.toString(), clientId);
		client.setCallback(callback);
		client.connect();
		client.subscribe("greetings");
	}
	
	@Deactivate
	public void stop() throws Exception {
		client.disconnect();
	}

}
