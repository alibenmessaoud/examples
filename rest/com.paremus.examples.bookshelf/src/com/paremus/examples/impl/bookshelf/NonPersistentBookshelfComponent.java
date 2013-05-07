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
package com.paremus.examples.impl.bookshelf;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import aQute.bnd.annotation.component.Component;

import com.paremus.examples.api.bookshelf.Book;
import com.paremus.examples.api.bookshelf.Bookshelf;

@Component(immediate = true)
public class NonPersistentBookshelfComponent implements Bookshelf {
	
	private final List<Book> books = new LinkedList<Book>();
	
	public NonPersistentBookshelfComponent() {
		// Populate with some dummy books
		books.add(new Book("Charles Dickens", "The Old Curiosity Shop"));
		books.add(new Book("Edwin A. Abbot", "Flatland"));
		books.add(new Book("Gabriel Garcia Marquez", "A Hundred Years of Solitude"));
	}

	public List<Book> listBooks() {
		return Collections.unmodifiableList(books);
	}

	public void add(Book book) {
		books.add(book);
	}

}
