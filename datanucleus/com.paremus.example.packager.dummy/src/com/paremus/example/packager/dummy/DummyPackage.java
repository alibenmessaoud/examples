package com.paremus.example.packager.dummy;

import aQute.bnd.annotation.component.Component;

import com.paremus.packager.whiteboard.api.PublishedApplication;

@Component(properties = { "uri=mongodb://localhost:27017" })
public class DummyPackage implements PublishedApplication {
}
