package com.paremus.example.datanucleus.service.rest;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.datanucleus.samples.blog.model.Comment;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.paremus.example.datanucleus.blog.api.Blog;

@Path("comments")
public class BlogCommentsResource {

    private final JsonFactory jsonFactory = new JsonFactory();

    @Context
    private UriInfo uriInfo;

    @Inject
    private Blog blog;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response list() throws Exception {
        StringWriter output = new StringWriter();
        
        List<Comment> comments = blog.listComments();
        JsonGenerator generator = jsonFactory.createJsonGenerator(output);
        
        generator.writeStartArray();
        for (Comment comment : comments) {
            generateCommentJson(generator, comment, true);
        }
        generator.writeEndArray();
        generator.close();
        
        return Response.status(Status.OK)
            .header(RFC2616.HEADER_ALLOW, HttpMethod.GET + "," + HttpMethod.POST)
            .type(MediaType.APPLICATION_JSON)
            .entity(output.toString())
            .build();
    }

    @GET @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("id") String id) throws Exception {
        Comment comment = blog.find(id);
        if (comment == null)
            return Response.status(Status.NOT_FOUND).build();
        
        StringWriter output = new StringWriter();
        JsonGenerator generator = jsonFactory.createJsonGenerator(output);
        generateCommentJson(generator, comment, false);
        generator.close();
        
        return Response.ok()
                .entity(output.toString())
                .type(MediaType.APPLICATION_JSON)
                .header(RFC2616.HEADER_ALLOW, HttpMethod.GET + "," + HttpMethod.PUT)
                .build();
    }

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    public Response create(String data) throws Exception {
        Comment comment = new Comment(UUID.randomUUID().toString(), data);
        blog.saveComment(comment);
        
        URI commentUri = uriInfo.getAbsolutePathBuilder().path(comment.id).build();
        return Response.created(commentUri).build();
    }

    @PUT @Path("{id}")
    @Consumes(MediaType.TEXT_PLAIN)
    public void save(@PathParam("id") String id, String data) throws Exception {
        Comment comment = blog.find(id);
        if (comment == null)
            comment = new Comment(id, data);
        else
            comment.text = data;
        blog.saveComment(comment);
    }

    @DELETE @Path("{id}")
    public Response delete(@PathParam("id") String id) throws Exception {
        blog.deleteComment(id);
        return Response.noContent().build();
    }

    private void generateCommentJson(JsonGenerator generator, Comment comment, boolean appendId) throws IOException, JsonGenerationException {
        generator.writeStartObject();
        generator.writeStringField("id", comment.id);
        generator.writeStringField("text", comment.text);
        
        generator.writeObjectFieldStart("link");
        UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder();
        if (appendId)
            uriBuilder.path(comment.id);
        generator.writeStringField("href", uriBuilder.build().toString());
        generator.writeEndObject();
        
        generator.writeEndObject();
    }
    
}
