package com.paremus.example.camel;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.datanucleus.samples.blog.model.Comment;
import org.osgi.framework.BundleContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;

import com.paremus.example.datanucleus.blog.api.Blog;

@Component
public class CamelSetupComponent {
	
	private Blog blog;
	private DefaultCamelContext context;

	@Reference
	public void setBlog(Blog blog) {
		this.blog = blog;
	}

	@Activate
	public void activate(BundleContext bc) throws Exception {
		SimpleRegistry registry = new SimpleRegistry();
		registry.put("blog", blog);
		registry.put("jsonToComment", new JacksonDataFormat(Comment.class));
		
		RouteBuilder routeBuilder = new RouteBuilder() {
			public void configure() throws Exception {
				from("file:INBOX?move=.done")
					.unmarshal("jsonToComment")
					.to("bean:blog?method=saveComment");
			}
		};
		
		context = new DefaultCamelContext(registry);
		context.addRoutes(routeBuilder);
		System.out.println("Starting Camel context");
		context.start();
	}
	
	@Deactivate
	public void deactivate() throws Exception {
		System.out.println("Stopping Camel context");
		context.stop();
	}

}