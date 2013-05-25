package com.paremus.examples.geiger;

import java.net.InetAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.bndtools.service.endpoint.Endpoint;
import org.eclipse.paho.client.mqttv3.MqttClient;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.ConfigurationPolicy;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;
import aQute.bnd.annotation.metatype.Configurable;
import aQute.bnd.annotation.metatype.Meta;

import com.paremus.examples.util.marshall.Marshaller;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

/**
 * <p>
 * This component is designed to run on a Raspberry Pi with the <a href=
 * "http://www.cooking-hacks.com/index.php/documentation/tutorials/geiger-counter-raspberry-pi-radiation-sensor-board"
 * >Radiation Sensor Board supplied by Cooking Hacks</a>.
 * </p>
 * 
 * <p>
 * It depends on the <a href="http://pi4j.com/">Pi4J library</a>.
 * </p>
 * 
 * @author Neil Bartlett <neil.bartlett@paremus.com>
 */
@Component(designateFactory = GeigerReaderComponent.Config.class, configurationPolicy = ConfigurationPolicy.optional, name = GeigerReaderComponent.NAME)
public class GeigerReaderComponent {
	
	static final String NAME = "com.paremus.examples.geiger";
	
	static interface Config {
		/**
		 * @see <a
		 *      href="http://www.cooking-hacks.com/index.php/documentation/tutorials/geiger-counter-raspberry-pi-radiation-sensor-board#cpm_to_servants">From
		 *      counts per minute to Sieverts</a>
		 */
		@Meta.AD(
				required = false,
				description = "CPM to µSieverts conversion factor (from GM tube specification)",
				deflt = "0.008120" // for J305ß tube
				)
		double conversionFactor();
	}
	
	private final String mqttClientId = Long.toString(System.currentTimeMillis());
	
	private final AtomicLong counter = new AtomicLong(0);
	private final Object checkLock = new Object();
	private String hostName;
	
	private String mqttServerUri = null;
	private MqttClient mqttClient = null;
	
	private ScheduledExecutorService scheduler;
	private GpioController gpio;
	private long clock = 0;
	
	@Reference(dynamic = true, optional = true)
	public void setEndpoint(Endpoint endpoint, Map<String, String> endpointProps) {
		synchronized (this) {
			mqttServerUri = endpointProps.get(Endpoint.URI);
			URI boundUri = URI.create(mqttServerUri);
			try {
				URI tcpUri = new URI("tcp", null, boundUri.getHost(), boundUri.getPort(), null, null, null);
				
				mqttClient = new MqttClient(tcpUri.toString(), mqttClientId);
				mqttClient.connect();
				System.out.println("Bound to MQTT server URI: " + boundUri);
			} catch (Exception e) {
				mqttServerUri = null;
				mqttClient = null;
			}
		}
	}
	public void unsetEndpoint(Endpoint endpoint, Map<String, String> endpointProps) {
		synchronized (this) {
			String endpointUri = endpointProps.get(Endpoint.URI);
			if (endpointUri.equals(mqttServerUri)) {
				mqttServerUri = null;
				if (mqttClient != null)
					try {
						mqttClient.disconnect();
					} catch (Exception e) {
						// Ignore
					}
				mqttClient = null;
				System.out.println("Disconnected from MQTT server URI: " + endpointUri);
			}
		}
	}

	@Activate
	void start(Map<String, Object> configProps) throws Exception {
		System.out.println("Starting Geiger Counter");
		
		hostName = InetAddress.getLocalHost().getHostName();
		
		final Config config = Configurable.createConfigurable(Config.class, configProps);
		final Runnable check = new Runnable() {
			public void run() {
				// Only one checker can run at a time
				synchronized (checkLock) {
					long now = System.currentTimeMillis();
					
					// get/reset counter and clock
					long ticks = counter.getAndSet(0L);
					long lastChecked = clock;
					clock = now;
					
					// discard first reading since we don't know its exact timing
					if (lastChecked != 0) {
						long period = now - lastChecked;
						
						double cpm = ((double) ticks * 60 * 1000) / (double) period;
						double usv = cpm * config.conversionFactor();
						
						System.out.printf("ticks=%d CPM=%f µSieverts=%f%n", ticks, cpm, usv);
						sendData(cpm, usv, now);
					} else {
						System.out.println("Discarding initial count");
					}
				}
			}
		};
		
		scheduler = Executors.newScheduledThreadPool(1);
		gpio = GpioFactory.getInstance();
		
		System.out.println("Opening GPIO listener...");
		
		// Prepare the input pin.
		// GPIO_01 == physical header 12 on the RPi == pin 18 on BCM2835 chip (used with /sys/class/gpio) 
		// See: https://projects.drogon.net/raspberry-pi/wiringpi/pins/
		GpioPinDigitalInput input = gpio.provisionDigitalInputPin(RaspiPin.GPIO_01);
		
		// Listen to digital state changes on the pin
		input.addListener(new GpioPinListenerDigital() {
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
				if (event.getState() == PinState.LOW) {
					long count = counter.incrementAndGet();
					if (count >= 50)
						scheduler.schedule(check, 0, TimeUnit.SECONDS);
				}
			}
		});
		
		System.out.println("Scheduling counter poll task...");

		scheduler.scheduleAtFixedRate(check, 0, 5, TimeUnit.SECONDS);
	}
	
	@Deactivate
	void stop() {
		System.out.println("Shutting down ");
		scheduler.shutdown();
		gpio.shutdown();
	}
	
	synchronized void sendData(double cpm, double usv, long time) {
		if (mqttClient != null) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("dose", usv);
			map.put("time", time);
			map.put("source", "Geiger Counter: " + hostName);
			try {
				mqttClient.getTopic("geiger").publish(Marshaller.marshal(map), 0, false);
			} catch (Exception e) {
				System.err.println("Error sending data to MQTT");
				e.printStackTrace();
			}
		}
	}


}