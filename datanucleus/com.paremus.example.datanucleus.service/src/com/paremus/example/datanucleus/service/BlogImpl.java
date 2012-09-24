package com.paremus.example.datanucleus.service;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;

import org.datanucleus.samples.blog.model.Comment;

import com.paremus.example.datanucleus.blog.api.Blog;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;

@Component
public class BlogImpl implements Blog {
    
    private PersistenceManagerFactory persistenceMgrFactory;
    private PersistenceManager persistenceMgr;

    @Reference
    public void setPersistenceManagerFactory(PersistenceManagerFactory persistenceMgrFactory) {
        this.persistenceMgrFactory = persistenceMgrFactory;
    }
    
    @Activate
    public void activate() {
        persistenceMgr = persistenceMgrFactory.getPersistenceManager();
    }
    
    @Deactivate
    public void deactivate() {
        persistenceMgr.close();
    }

    public List<Comment> listComments() throws Exception {
        Query query = persistenceMgr.newQuery(Comment.class);
        try {
            @SuppressWarnings("unchecked")
            List<Comment> comments = (List<Comment>) query.execute();

            return new ArrayList<Comment>(comments);
        } finally {
            query.closeAll();
        }
    }

    public void saveComment(Comment comment) throws Exception {
        Transaction tx = persistenceMgr.currentTransaction();
        try {
            tx.begin();
            persistenceMgr.makePersistent(comment);
            tx.commit();
        } finally {
            if (tx.isActive())
                tx.rollback();
        }
    }
    
    public Comment find(String id) throws Exception {
        // This should be implemented with a WHERE query using JDOQL, but I couldn't get it to work.
        // The following solution is simple but would be slow with a large table.
        Query query = persistenceMgr.newQuery(Comment.class);
        
        @SuppressWarnings("unchecked")
        List<Comment> comments = (List<Comment>) query.execute();
        for (Comment comment : comments) {
            if (comment.id.equals(id))
                return comment;
        }
        return null;
    }
    
    public void deleteComment(String id) throws Exception {
        Comment comment = find(id);
        if (comment != null) {
            persistenceMgr.deletePersistent(comment);
        }
    }

}
