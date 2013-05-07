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
package com.paremus.examples.api.bookshelf;

import java.util.List;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface Bookshelf {

	List<Book> listBooks();
	
	void add(Book book);
	
}
