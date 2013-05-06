package com.paremus.examples.mqtt.server;

import java.net.URI;
import java.util.Hashtable;
import java.util.Map;

import org.bndtools.service.endpoint.Endpoint;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;

/**
 * Subscribes to MQTT server and republishes messages from the topic "geiger" to
 * the Event Admin topic "TELEMETRY/RADIATION"
 * 
 * @author Neil Bartlett <neil.bartlett@paremus.com>
 */
@Component
public class TopicSubscriber {

	private final String clientId = Long.toString(System.currentTimeMillis());
	
	private String mqttUri;
	private MqttClient client;
	private EventAdmin eventAdmin;

	@Reference(target = "(uri=mqtt://*)")
	public void setEndpoint(Endpoint endpoint, Map<String, String> endpointProps) {
		mqttUri = endpointProps.get(Endpoint.URI);
		System.out.println("[subscriber]: bound to MQTT server URI: " + mqttUri);
	}
	
	@Reference
	public void setEventAdmin(EventAdmin eventAdmin) {
		this.eventAdmin = eventAdmin;
	}
	
	@Activate
	public void start() throws Exception {
		URI boundUri = URI.create(mqttUri);
		URI tcpUri = new URI("tcp", null, boundUri.getHost(), boundUri.getPort(), null, null, null);
		
		MqttCallback callback = new MqttCallback() {
			@Override
			public void messageArrived(MqttTopic topic, MqttMessage message) throws Exception {
				String messageStr = new String(message.getPayload());
				double dose = Double.parseDouble(messageStr);
				
				System.out.printf("[subscriber]: received dose value %f over MQTT, forwarding to EventAdmin%n", dose);
				Map<String, Object> eventProps = new Hashtable<String, Object>();
				eventProps.put("dose", dose);
				eventAdmin.postEvent(new Event("TELEMETRY/RADIATION", eventProps));
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
		client.subscribe("geiger");
	}
	
	@Deactivate
	public void stop() throws Exception {
		client.disconnect();
	}

}
