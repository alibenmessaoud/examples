/*******************************************************************************
 * Copyright (c) 2012 "Neil Bartlett, Paremus Ltd" <neil.bartlett@paremus.com>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     "Neil Bartlett, Paremus Ltd" <neil.bartlett@paremus.com> - initial API and implementation
 ******************************************************************************/
package com.paremus.examples.impl.site;

import org.osgi.service.http.HttpService;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;

@Component
public class SiteRegistrationComponent {
	
	private HttpService http;

	@Reference
	void bindHttpService(HttpService http) {
		this.http = http;
	}
	
	@Activate
	void activate() throws Exception {
		http.registerResources("/", "static", null);
	}
	
	@Deactivate
	void deactivate() throws Exception {
		http.unregister("/");
	}

}
