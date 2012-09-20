package com.paremus.example.datanucleus.blog.api;

import java.util.List;

import org.datanucleus.samples.blog.model.Comment;

public interface Blog {
    
    List<Comment> listComments() throws Exception;
    
    void saveComment(Comment comment) throws Exception;
    
    Comment find(String id) throws Exception;

}
