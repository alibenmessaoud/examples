package com.paremus.examples.websocket.server;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;

@Component(
		immediate = true,
		properties = "event.topics=TELEMETRY/RADIATION")
public class WebSocketServerComponent implements EventHandler {
	
	private final int port = 8000; // TODO: make this configurable
	
	private ServerBootstrap server;
	private WebSocketServerHandler wshandler;

	@Activate
	void start() {
		server = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
		wshandler = new WebSocketServerHandler();
		server.setPipelineFactory(new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() throws Exception {
		        ChannelPipeline pipeline = Channels.pipeline();
		        
		        pipeline.addLast("decoder", new HttpRequestDecoder());
		        pipeline.addLast("aggregator", new HttpChunkAggregator(65536));
		        pipeline.addLast("encoder", new HttpResponseEncoder());
				pipeline.addLast("handler", wshandler);
				
		        return pipeline;
			}
		});
		server.bind(new InetSocketAddress(port));
		System.out.printf("[websocket] started server on port %d%n", port);
	}
	
	@Deactivate
	void stop() {
		server.shutdown();
		System.out.println("[websocket]: shutdown server");
	}

	@Override
	public void handleEvent(Event event) {
		System.out.println("[websocket]: received event on topic " + event.getTopic());
		Object doseObj = event.getProperty("dose");
		if (doseObj != null && doseObj instanceof Number) {
			double dose = ((Number) doseObj).doubleValue();
			wshandler.sendText(String.format("Current radiation level is %.3f uSv", dose));
		}
	}
	
}
