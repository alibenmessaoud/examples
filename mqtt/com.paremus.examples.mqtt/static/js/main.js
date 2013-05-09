angular.module("MQTTDemo", []);

function SensorDataCtrl ($scope) {
	
	$scope.state = "waiting";

	if (!!window.EventSource) {
		var source = new EventSource("/events");
		source.onmessage = function(e) {
			$scope.state = "received";
			$scope.data = JSON.parse(e.data);
			$scope.$apply();
		};
		source.onerror = function(e) {
			$scope.state = "error";
			$scope.message = "Disconnected";
			$scope.$apply();
		};
	} else {
		$scope.state = "error";
		$scope.message = "Your browser does not support HTML5 Server-Sent Events";
	}

	
}