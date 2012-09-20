package com.paremus.example.datanucleus.service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.felix.service.command.Descriptor;
import org.datanucleus.samples.blog.model.Comment;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;

import com.paremus.example.datanucleus.blog.api.Blog;

@Component(
        provide = Object.class,
        immediate = true,
        properties = {
            "osgi.command.scope=blog",
            "osgi.command.function=list|create"
        })
public class BlogCommands {
    
    private final AtomicReference<Blog> blogRef = new AtomicReference<Blog>();
    
    @Reference(optional = true, dynamic = true)
    public void setBlog(Blog blog) {
        blogRef.set(blog);
    }
    public void unsetBlog(Blog blog) {
        blogRef.compareAndSet(blog, null);
    }
    
    private Blog getBlog() {
        Blog blog = blogRef.get();
        if (blog == null)
            throw new RuntimeException("Blog service not available");
        return blog;
    }

    @Descriptor("List blog comments")
    public void list() throws Exception {
        List<Comment> comments = getBlog().listComments();
        System.out.printf("Listing %d comments:\n", comments.size());
        for (Comment comment : comments) {
            System.out.printf("%tc :: %s\n", comment.created, comment.text);
        }
        System.out.println("END");
    }
    
    @Descriptor("Create new blog comment")
    public void create(@Descriptor("Comment text") String text) throws Exception {
        Comment comment = new Comment(UUID.randomUUID().toString(), text);
        getBlog().saveComment(comment);
    }

}
