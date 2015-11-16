'use strict';

// Declare app level module which depends on views, and components
var fedeApp = angular.module('fedeApp', [
    'ngRoute',
    'fedeApp.i18n',
    'fedeApp.version',
    'fedeApp.group',
    'fedeApp.inbox'
]).
        config(['$routeProvider', function ($routeProvider) {
                $routeProvider.otherwise({redirectTo: 'inbox'});
            }]);
