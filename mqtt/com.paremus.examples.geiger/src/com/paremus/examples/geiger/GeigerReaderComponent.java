package com.paremus.examples.geiger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;

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
@Component
public class GeigerReaderComponent {

	private final AtomicInteger counter = new AtomicInteger(0);
	
	private ScheduledExecutorService scheduler;
	private GpioController gpio;

	@Activate
	void start() {
		scheduler = Executors.newScheduledThreadPool(1);
		gpio = GpioFactory.getInstance();
		
		// Prepare the input pin.
		// GPIO_01 == physical header 12 on the RPi == pin 18 on BCM2835 chip (used with /sys/class/gpio) 
		// See: https://projects.drogon.net/raspberry-pi/wiringpi/pins/
		GpioPinDigitalInput input = gpio.provisionDigitalInputPin(RaspiPin.GPIO_01);
		
		// Listen to digital state changes on the pin
		input.addListener(new GpioPinListenerDigital() {
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
				if (event.getState() == PinState.LOW)
					counter.incrementAndGet();
			}
		});
		
		// Poll the count every 15 seconds
		scheduler.scheduleAtFixedRate(new Runnable() {
			public void run() {
				int ticks = counter.getAndSet(0);
				System.out.printf("ticks=%d CPM=%d Sieverts= %n", ticks);
			}
		}, 15, 15, TimeUnit.SECONDS);
	}
	
	@Deactivate
	void stop() {
		gpio.shutdown();
	}


}