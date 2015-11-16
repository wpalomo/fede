'use strict';

describe('fedeApp.version module', function() {
  beforeEach(module('fedeApp.version'));

  describe('version service', function() {
    it('should return current version', inject(function(version) {
      expect(version).toEqual('0.1');
    }));
  });
});
