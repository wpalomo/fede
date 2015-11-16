'use strict';

angular.module('fedeApp.version', [
  'fedeApp.version.interpolate-filter',
  'fedeApp.version.version-directive'
])

.value('version', '0.1');
