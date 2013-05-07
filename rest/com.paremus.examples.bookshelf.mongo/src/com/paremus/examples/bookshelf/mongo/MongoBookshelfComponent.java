package com.paremus.examples.bookshelf.mongo;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bndtools.service.endpoint.Endpoint;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoURI;
import com.paremus.examples.api.bookshelf.Book;
import com.paremus.examples.api.bookshelf.Bookshelf;

@Component(immediate = true)
public class MongoBookshelfComponent implements Bookshelf {
	
	private MongoURI boundUri;
	private Mongo mongo;
	private DB db;
	
	@Reference(target = "(uri=mongodb:*)")
	void setEndpoint(Endpoint endpoint, Map<String, String> endpointProps) {
		boundUri = new MongoURI(endpointProps.get(Endpoint.URI));
	}
	
	@Activate
	public void activate() throws Exception {
		mongo = boundUri.connect();
		db = mongo.getDB("test");
		System.out.printf("Connected to MongoDB instance at address %s, database %s.%n", boundUri, db.getName());
	}
	
	@Deactivate
	public void deactivate() {
		mongo.close();
		System.out.println("Disconnected from " + boundUri);
	}

	public void add(Book book) {
		DBObject object = new BasicDBObject();
		object.put("author", book.getAuthor());
		object.put("title", book.getTitle());
		
		DBCollection coll = db.getCollection("books");
		coll.insert(object);
	}
	
	public List<Book> listBooks() {
		List<Book> books = new LinkedList<Book>();
		
		DBCollection coll = db.getCollection("books");
		for (DBObject object : coll.find()) {
			Book book = new Book((String) object.get("author"), (String) object.get("title"));
			books.add(book);
		}
		
		return books;
	}

}