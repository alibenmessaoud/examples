package com.paremus.examples.mqtt.client;

import org.bndtools.service.endpoint.Endpoint;

import aQute.bnd.annotation.component.Component;

@Component(properties = { "service.exported.interfaces=*", "uri=foobar" })
public class EndpointProvider implements Endpoint {
}
