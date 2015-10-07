'use strict';

/* Services */

/*
 http://docs.angularjs.org/api/ngResource.$resource

 Default ngResources are defined as

 'get':    {method:'GET'},
 'save':   {method:'POST'},
 'query':  {method:'GET', isArray:true},
 'remove': {method:'DELETE'},
 'delete': {method:'DELETE'}

 */

var services = angular.module('ngdemo.services', ['ngResource']);

//services.factory('DummyFactory', function ($resource) {
//    return $resource('/ngdemo/web/dummy', {}, {
//        query: { method: 'GET', params: {}, isArray: false }
//    })
//});

//services.factory('UsersFactory', function ($resource) {
//    return $resource('/webresources/factura/lista/:subjectId/:tags/:start/:end', {}, {
//        query: { method: 'GET', isArray: true },
//        create: { method: 'POST' }
//    })
//});
services.factory('UsersFactory', function ($resource) {
    return $resource('/fede-web/webresources/facturas/91/fede/1412656104306/1444105704306', {}, {
        query: { method: 'GET', isArray: true },
        create: { method: 'POST' }
    });
});

//services.factory('UserFactory', function ($resource) {
//    return $resource('/ngdemo/web/users/:id', {}, {
//        show: { method: 'GET' },
//        update: { method: 'PUT', params: {id: '@id'} },
//        delete: { method: 'DELETE', params: {id: '@id'} }
//    })
//});
