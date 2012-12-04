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

app.factory("Endpoints", function($resource) {
	return $resource("/endpoints");
});

function LibraryCtrl($scope, Endpoints, $http) {
	
	$scope.endpoints = Endpoints.query();
	
	$scope.books = [];
	
	$scope.listBooks = function() {
		var uri = $scope.selectedEndpoint;
		if (uri) {
			$http.get(uri).success(function(data, status) {
				$scope.books = data;
			}).error(function(data, status) {
				alert ("Failed to retrieve book list: " + data);
			});
		} else {
			$scope.books = [];
		}
	}
	
	$scope.addBook = function() {
		var book = {
				author : newBook.author.value,
				title : newBook.title.value
		};
		
		$http.post($scope.selectedEndpoint, book).success(function() {
			$scope.books.push(book);
			
			newBook.author.value = '';
			newBook.title.value = '';
		}).error(function(data, status) {
			alert ("Failed to post new book: " + data);
		});
	}
	
}
