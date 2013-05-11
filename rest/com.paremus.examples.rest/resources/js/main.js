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

var app = angular.module("demo", []);

function BookshelfCtrl($scope, $http) {
	
	$scope.endpointState = "waiting";
	$scope.endpoints = [];
	
	$scope.selectedEndpoint = null;
	$scope.books = null;
	
	if (!!window.EventSource) {
		var source = new EventSource("/events");
		
		source.onmessage = function (e) {
			$scope.endpointState = "received";
			var message = JSON.parse(e.data);
			if (message.operation === "add") {
				$scope.endpoints = $scope.endpoints.concat([message.uri]);
			} else if (message.operation === "remove") {
				arrayRemove($scope.endpoints, message.uri);
			}
			$scope.$apply();
		};
		
		source.onerror = function (e) {
			$scope.endpointState = "error";
			$scope.message = "Disconnected";
			$scope.endpoints = [];
			$scope.$apply();
		};
		
	} else {
		$scope.endpointState = "error";
		$scope.message = "Your browser does not support HTML5 Server-Sent Events.";
	}
	
	$scope.selectEndpoint = function (uri) {
		$scope.selectedEndpoint = uri;
		if (uri) {
			$http.get(uri).success(function(data, status) {
				$scope.books = data;
			}).error(function(data, status) {
				alert ("Failed to retrieve book list: " + data);
			});
		} else {
			$scope.books = null;
		}
	}
	
	$scope.findSelectedEndpointClass = function (endpoint) {
		if (endpoint === $scope.selectedEndpoint)
			return "well-highlight";
		else
			return "well";
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

function indexOf(array, obj) {
	if (array.indexOf)
		return array.indexOf(obj);
	for ( var i = 0; i < array.length; i++) {
		if (obj === array[i])
			return i;
	}
	return -1;
}

function arrayRemove(array, value) {
	var index = indexOf(array, value);
	if (index >= 0)
		array.splice(index, 1);
	return value;
}
