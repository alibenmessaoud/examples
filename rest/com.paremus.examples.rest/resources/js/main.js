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

var app = angular.module("demo", ["ngResource"]);

app.factory("Books", function($resource) {
	return $resource("/library/");
});

function LibraryCtrl($scope, Books) {
	
	$scope.books = Books.query();
	
	$scope.addBook = function() {
		var book = {
				author : newBook.author.value,
				title : newBook.title.value
		};
		Books.save(book, function() {
			$scope.books.push(book);
			newBook.author.value = '';
			newBook.title.value = '';
		});
	}
	
}
