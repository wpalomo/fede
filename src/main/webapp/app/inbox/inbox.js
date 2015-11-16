'use strict';

angular.module('fedeApp.inbox', ['ngRoute'])

.config(['$routeProvider', function($routeProvider) {
  $routeProvider.when('/inbox/:groupId', {
    templateUrl: 'inbox/inbox.html',
    controller: 'InboxCtrl'
  });
}])

.controller('InboxCtrl', [function() {

}]);