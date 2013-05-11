package com.paremus.examples.impl.site;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.Executors;

import org.bndtools.service.endpoint.Endpoint;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.codec.string.StringEncoder;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;

@Component
public class StreamingEndpointListingComponent {

	private final int port = 8000; // TODO: make this configurable
	private final StreamingEndpointListingHttpHandler httpHandler = new StreamingEndpointListingHttpHandler();
	
	private ServerBootstrap server;
	
	@Reference(type = '*', target = "(uri=http://*/bookshelf)")
	void bindEndpoint(Endpoint endpoint, Map<String, String> props) throws Exception {
		String uri = props.get(Endpoint.URI);
		httpHandler.addEndpoint(uri);
	}
	void unbindEndpoint(Endpoint endpoint, Map<String, String> props) throws Exception {
		String uri = props.get(Endpoint.URI);
		httpHandler.removeEndpoint(uri);
	}


	@Activate
	void start() throws Exception {
		server = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
		server.setPipelineFactory(new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline pipeline = Channels.pipeline();
				
		        pipeline.addLast("decoder", new HttpRequestDecoder());
		        pipeline.addLast("aggregator", new HttpChunkAggregator(65536));
		        pipeline.addLast("encoder", new HttpResponseEncoder());
		        pipeline.addLast("sse_encoder", new StringEncoder());
				pipeline.addLast("handler", httpHandler);
				
		        return pipeline;
			}
		});
		server.bind(new InetSocketAddress(port));
		System.out.printf("Started bookshelf HTTP server on port %d.%n", port);
	}
	
	@Deactivate
	void stop() {
		server.shutdown();
		System.out.println("Stopped bookshelf HTTP server");
	}
}
