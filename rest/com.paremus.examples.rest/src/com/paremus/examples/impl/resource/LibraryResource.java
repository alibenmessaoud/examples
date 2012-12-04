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
package com.paremus.examples.impl.resource;

import java.io.InputStream;
import java.io.StringWriter;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paremus.examples.api.library.Book;
import com.paremus.examples.api.library.Library;

@Path("/")
public class LibraryResource {
	
	@Inject
	Library library;
	
	private ResponseBuilder addAccessControlHeaders(ResponseBuilder rb) {
		return rb.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "OPTIONS, GET, POST")
				.header("Access-Control-Allow-Headers", "X-Requested-With, Content-Type");
	}
	
	@OPTIONS
	public Response getOptions() {
		return addAccessControlHeaders(Response.ok()).build();
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response listBooks() throws Exception {
		StringWriter writer = new StringWriter();
		new ObjectMapper().writeValue(writer, library.listBooks());
		
		return addAccessControlHeaders(Response.ok(writer.toString())).build();
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postBook(InputStream data) throws Exception {
		Book book = new ObjectMapper().readValue(data, Book.class);
		library.add(book);
		return addAccessControlHeaders(Response.noContent()).build();
	}
	
}
