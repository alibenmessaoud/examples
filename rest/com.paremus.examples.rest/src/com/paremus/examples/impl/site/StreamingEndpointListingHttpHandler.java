package com.paremus.examples.impl.site;

import static org.jboss.netty.handler.codec.http.HttpHeaders.*;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.*;
import static org.jboss.netty.handler.codec.http.HttpMethod.*;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.*;
import static org.jboss.netty.handler.codec.http.HttpVersion.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders.Values;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.util.CharsetUtil;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

public class StreamingEndpointListingHttpHandler extends SimpleChannelUpstreamHandler {

    private static final String EVENTS_PATH = "/events";

    private final Object lock = new Object();
    private final List<Channel> clients = new LinkedList<Channel>();
    private final List<String> endpoints = new LinkedList<String>();
    
    void addEndpoint(String endpoint) {
    	synchronized (lock) {
			endpoints.add(endpoint);
			sendToAllClients(generateMessage("add", endpoint));
		}
    }
    
    void removeEndpoint(String endpoint) {
    	synchronized (lock) {
    		endpoints.remove(endpoint);
    		sendToAllClients(generateMessage("remove", endpoint));
		}
    }
    
    void addClient(Channel client) {
    	System.out.printf("Adding client: %s%n", client);
    	synchronized (lock) {
			clients.add(client);
			for (String endpoint : endpoints) {
				String message = "data: " + generateMessage("add", endpoint) + "\n\n";
				client.write(message);
			}
		}
    }
    
	private String generateMessage(String operation, String uri) {
		try {
			StringWriter writer = new StringWriter();
			JsonGenerator generator = new JsonFactory().createJsonGenerator(writer);
			generator.writeStartObject();
			generator.writeStringField("operation", operation);
			generator.writeStringField("uri", uri);
			generator.writeEndObject();
			generator.close();
			return writer.toString();
		} catch (IOException e) {
			// shouldn't happen
			throw new RuntimeException(e);
		}
	}

	private void sendToAllClients(Object message) {
		List<Channel> toRemove = new LinkedList<Channel>();
		synchronized (lock) {
			for (Iterator<Channel> iter = clients.iterator(); iter.hasNext(); ) {
				Channel channel = iter.next();
				if (channel.isOpen()) {
					String messageStr = new StringBuilder().append("data: ").append(message).append("\n\n").toString();
					channel.write(messageStr);
				} else {
					toRemove.add(channel);
				}
			}
			clients.removeAll(toRemove);
		}
	}
	
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        Object msg = e.getMessage();
        if (msg instanceof HttpRequest) {
            handleHttpRequest(ctx, (HttpRequest) msg);
        }
    }

	private void handleHttpRequest(ChannelHandlerContext ctx, HttpRequest req) throws Exception {
        if (req.getMethod() != GET) {
            sendHttpResponse(ctx, req, new DefaultHttpResponse(HTTP_1_1, FORBIDDEN));
            return;
        }
        
        String path = req.getUri();
        if ("/".equals(path))
        	path = "/index.html";
        
        if (EVENTS_PATH.equals(path)) {
        	// respond to request path "/events" by opening an event stream
        	HttpResponse res = new DefaultHttpResponse(HTTP_1_1, OK);
        	res.setHeader(CONTENT_TYPE, "text/event-stream");
        	res.setHeader(CACHE_CONTROL, Values.NO_CACHE);
        	ChannelFuture f = ctx.getChannel().write(res);
        	f.addListener(new ChannelFutureListener() {
				public void operationComplete(ChannelFuture f) throws Exception {
					if (f.isSuccess())
						addClient(f.getChannel());
				}
			});
        } else {
        	// map all other uris to the static resources
        	String resourcePath = "static" + path;
        	InputStream stream = getClass().getClassLoader().getResourceAsStream(resourcePath);
        	if (stream != null) {
                HttpResponse res = new DefaultHttpResponse(HTTP_1_1, OK);

        		ChannelBuffer content = collect(stream);
                setContentLength(res, content.readableBytes());

                res.setContent(content);
                sendHttpResponse(ctx, req, res);
        	} else {
        		System.out.printf("Failed to map request path %s (tried resource %s).%n", path, resourcePath);
        		sendHttpResponse(ctx, req, new DefaultHttpResponse(HTTP_1_1, NOT_FOUND));
        	}
        }
    }
	
    private static void sendHttpResponse(ChannelHandlerContext ctx, HttpRequest req, HttpResponse res) {
        // Generate an error page if response status code is not OK (200).
        if (res.getStatus().getCode() != 200) {
            res.setContent(ChannelBuffers.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8));
            setContentLength(res, res.getContent().readableBytes());
        }

        // Send the response and close the connection if necessary.
        ChannelFuture f = ctx.getChannel().write(res);
        if (!isKeepAlive(req) || res.getStatus().getCode() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private ChannelBuffer collect(InputStream stream) throws IOException {
    	ChannelBuffer cb = ChannelBuffers.dynamicBuffer(1024);
    	try {
    		byte[] tmp = new byte[1024];
    		int bytesRead = stream.read(tmp);
    		while (bytesRead >= 0) {
    			cb.writeBytes(tmp, 0, bytesRead);
    			bytesRead = stream.read(tmp);
    		}
    		return cb;
    	} finally {
    		stream.close();
    	}
    }
    
}
