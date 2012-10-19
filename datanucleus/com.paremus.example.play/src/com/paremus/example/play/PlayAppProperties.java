package com.paremus.example.play;

import aQute.bnd.annotation.metatype.Meta;

public interface PlayAppProperties {

	@Meta.AD(required = false, deflt = "9999", description = "HTTP server port")
	int httpPort();
	
	@Meta.AD(required = true, description = "URL of midtier REST endpoint")
	String restUrl();

}
