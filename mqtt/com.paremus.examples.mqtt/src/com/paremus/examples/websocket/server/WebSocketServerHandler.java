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
package com.paremus.examples.websocket.server;

import static org.jboss.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static org.jboss.netty.handler.codec.http.HttpHeaders.setContentLength;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.HOST;
import static org.jboss.netty.handler.codec.http.HttpMethod.GET;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import org.jboss.netty.logging.InternalLogger;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.util.CharsetUtil;
import org.json.JSONObject;

import com.floreysoft.jmte.Engine;

import aQute.lib.io.IO;

/**
 * Handles handshakes and messages
 */
public class WebSocketServerHandler extends SimpleChannelUpstreamHandler {
	
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(WebSocketServerHandler.class);
    private static final String WEBSOCKET_PATH = "/websocket";

    private final Engine templateEngine;
    
    private final List<Channel> clients = new CopyOnWriteArrayList<Channel>();
    private WebSocketServerHandshaker handshaker;
    
    public WebSocketServerHandler() {
    	templateEngine = new Engine();
    	templateEngine.setUseCompilation(false);
	}

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        Object msg = e.getMessage();
        if (msg instanceof HttpRequest) {
            handleHttpRequest(ctx, (HttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }
    
    public void sendAsString(Object object) {
    	for (Iterator<Channel> iter = clients.iterator(); iter.hasNext(); ) {
    		Channel channel = iter.next();
    		if (channel.isOpen())
    			channel.write(new TextWebSocketFrame(object.toString()));
    		else
    			iter.remove();
    	}
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, HttpRequest req) throws Exception {
        // Allow only GET methods.
        if (req.getMethod() != GET) {
            sendHttpResponse(ctx, req, new DefaultHttpResponse(HTTP_1_1, FORBIDDEN));
            return;
        }
        
        if (WEBSOCKET_PATH.equals(req.getUri())) {
        	handshakeWebSocket(ctx, req);
        } else if ("/".equals(req.getUri())) {
        	// Generate the index page
            HttpResponse res = new DefaultHttpResponse(HTTP_1_1, OK);

            String input= IO.collect(getClass().getClassLoader().getResourceAsStream("templates/index.html"), "UTF-8");
            Map<String, Object> templateModel = new HashMap<String, Object>();
            templateModel.put("WebSocketLocation", getWebSocketLocation(req));
			String data = templateEngine.transform(input, templateModel);
            
            ChannelBuffer content = ChannelBuffers.wrappedBuffer(data.getBytes());

            res.setHeader(CONTENT_TYPE, "text/html; charset=UTF-8");
            setContentLength(res, content.readableBytes());

            res.setContent(content);
            sendHttpResponse(ctx, req, res);
        } else {
        	// map all other urls to the static resources
        	String path = "resources" + req.getUri();
        	InputStream stream = getClass().getClassLoader().getResourceAsStream(path);
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
    
	private void handshakeWebSocket(ChannelHandlerContext ctx, HttpRequest req) {
		WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(req), null, false);
		handshaker = wsFactory.newHandshaker(req);
		if (handshaker == null) {
			wsFactory.sendUnsupportedWebSocketVersionResponse(ctx.getChannel());
		} else {
			ChannelFutureListener channelListener = new ChannelFutureListener() {
				public void operationComplete(ChannelFuture future)
						throws Exception {
					Channel channel = future.getChannel();
					if (!future.isSuccess()) {
						Channels.fireExceptionCaught(channel,future.getCause());
					}
					clients.add(channel);
				}
			};
			handshaker.handshake(ctx.getChannel(), req).addListener(channelListener);
		}
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        // Check for closing frame
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.getChannel(), (CloseWebSocketFrame) frame);
            clients.remove(ctx.getChannel());
            return;
        }
        if (frame instanceof PingWebSocketFrame) {
            ctx.getChannel().write(new PongWebSocketFrame(frame.getBinaryData()));
            return;
        }
        if (!(frame instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass()
                    .getName()));
        }

        // Send the uppercase string back.
        String request = ((TextWebSocketFrame) frame).getText();
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Channel %s received %s", ctx.getChannel().getId(), request));
        }
        ctx.getChannel().write(new TextWebSocketFrame(request.toUpperCase()));
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

    private static String getWebSocketLocation(HttpRequest req) {
        return "ws://" + req.getHeader(HOST) + WEBSOCKET_PATH;
    }
}