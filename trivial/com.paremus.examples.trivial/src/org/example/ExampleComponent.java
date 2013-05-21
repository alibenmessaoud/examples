package org.example;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;

@Component
public class ExampleComponent {

	@Activate
	void activate() {
		System.out.println("Started ExampleComponent");
	}
	
	@Deactivate
	void deactivate() {
		System.out.println("Stopped ExampleComponent");
	}

}