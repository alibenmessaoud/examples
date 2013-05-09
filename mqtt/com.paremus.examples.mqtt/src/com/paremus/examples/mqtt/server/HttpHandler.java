/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.paremus.examples.mqtt.server;

import static org.jboss.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static org.jboss.netty.handler.codec.http.HttpHeaders.setContentLength;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CACHE_CONTROL;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static org.jboss.netty.handler.codec.http.HttpMethod.GET;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.util.CharsetUtil;

/**
 * Handles HTTP Requests
 */
public class HttpHandler extends SimpleChannelUpstreamHandler {
	
    private static final String EVENTS_PATH = "/events";

    private final List<Channel> clients = new CopyOnWriteArrayList<Channel>();
    
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        Object msg = e.getMessage();
        if (msg instanceof HttpRequest) {
            handleHttpRequest(ctx, (HttpRequest) msg);
        }
    }
    
    public void sendAsString(Object object) {
    	List<Channel> toRemove = new LinkedList<Channel>();
    	
    	for (Iterator<Channel> iter = clients.iterator(); iter.hasNext(); ) {
    		Channel channel = iter.next();
    		if (channel.isOpen()) {
    			String message = new StringBuilder().append("data: ").append(object).append("\n\n").toString();
    			channel.write(message);
    		} else {
    			toRemove.add(channel);
    		}
    	}
    	
    	clients.removeAll(toRemove);
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
        	HttpResponse res = new DefaultHttpResponse(HTTP_1_1, OK);
        	res.setHeader(CONTENT_TYPE, "text/event-stream");
        	res.setHeader(CACHE_CONTROL, HttpHeaders.Values.NO_CACHE);
        	ChannelFuture f = ctx.getChannel().write(res);
        	f.addListener(new ChannelFutureListener() {
				public void operationComplete(ChannelFuture f) throws Exception {
					if (f.isSuccess())
						clients.add(f.getChannel());
				}
			});
        } else {
        	// map all other urls to the static resources
        	String resourcePath = "resources" + path;
        	InputStream stream = getClass().getClassLoader().getResourceAsStream(resourcePath);
        	if (stream != null) {
                HttpResponse res = new DefaultHttpResponse(HTTP_1_1, OK);

        		ChannelBuffer content = collect(stream);
                setContentLength(res, content.readableBytes());

                res.setContent(content);
                sendHttpResponse(ctx, req, res);
        	} else {
        		sendHttpResponse(ctx, req, new DefaultHttpResponse(HTTP_1_1, NOT_FOUND));
        	}
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

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        e.getCause().printStackTrace();
        e.getChannel().close();
    }

}
