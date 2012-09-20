package org.datanucleus.samples.blog.model;

import java.util.Date;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable
public class Comment {

	@PrimaryKey
	public String id;

	public String text;
	public Date created;

	public Comment() {
	}

	public Comment(String id, String text) {
		this.id = id;
		this.text = text;

		this.created = new Date();
	}
}