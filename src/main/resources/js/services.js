'use strict';

/* Services */

var adminHelperServices = angular.module('adminHelper.services', ['ngResource']);

adminHelperServices.factory('Hardware', ['ngResource',
    function ($resource) {
        return $resource('hardware/:hardwareId/device/:deviceId', {}, {
            query: {method: 'GET', params: {}, isArray: true}
        });
    }]);
