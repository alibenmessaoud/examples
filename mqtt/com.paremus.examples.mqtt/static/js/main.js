angular.module("MQTTDemo", []);

function SensorDataCtrl ($scope) {
	
	$scope.state = "waiting";

	var socket;
	if (!window.WebSocket) {
		window.WebSocket = window.MozWebSocket;
	}
	if (window.WebSocket) {
		socket = new WebSocket(webSocketLocation);
		socket.onmessage = function(event) {
			$scope.state = "received";
			$scope.value = event.data;
			$scope.$apply();
		};
		socket.onclose = function(event) {
			$scope.state = "error";
			$scope.message = "Web Socket closed";
			$scope.$apply();
		};
	} else {
		$scope.state = "error";
		$scope.message = "Your browser does not support Web Socket.";
	}

	
}