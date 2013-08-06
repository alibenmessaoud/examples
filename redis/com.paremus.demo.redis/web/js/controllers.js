'use strict';

/* Filters */
angular.module('statusMonitor', []).filter('statusMessage', function() {
  return function(currentPrices) {
    if(currentPrices.availability ==='NONE') {
      return 'Redis store is currently unavailable';
    } else if (currentPrices.availability ==='READ') {
      return 'Redis store is currently READ ONLY. Data retrieved from ' + currentPrices.fromURI;
    } else if (currentPrices.availability ==='WRITE') {
      return 'Redis store is healthy. Data retrieved from ' + currentPrices.fromURI;
    }
    return 'Redis state is unknown: ' + currentPrices.availability;
  };
}).filter('styleRow', function() {
  return function(startPrice, currentPrice) {
    if(startPrice < currentPrice) {
      return 'success';
    }
    if(startPrice > currentPrice) {
      return 'error';
    }
    return 'info';
  };
}).filter('styleSymbol', function() {
  return function(startPrice, currentPrice) {
    if(startPrice < currentPrice) {
      return 'icon-arrow-up';
    }
    if(startPrice > currentPrice) {
      return 'icon-arrow-down';
    }
    return 'icon-arrow-right';
  };
}).filter('toCurrency', function() {
  return function(price) {
    return price/10 + ' pence';
  };
}).filter('toChange', function() {
  return function(startPrice, currentPrice) {
    var delta = currentPrice - startPrice;
    var percentage = (delta/startPrice) * 100;
    
    return (delta/10).toFixed(1) + ' pence (' + percentage.toFixed(2) +  '%)';
  };
});

/* Controllers */

function RedisDemoCtl($scope, $http, $timeout) {
  
  (function init() {
    $http.get('rest', { params : { openingPrices : true } }).success(function(data) {
      if(data.availability === 'NONE') {
        $timeout(init, 2000);
      } else {
        $scope.currentPrices = data;
        $scope.openingPrices = data;
      
        (function poll() {
          $http.get('rest').success(function(data) {
            $scope.currentPrices = data;
          }).always(function() {
            $timeout(poll, 2000);
          });
        })();
      }
    }).error(function() {
      $timeout(init, 2000);
    });
  })();

}
