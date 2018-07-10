/// <reference path="apimanPlugin.ts"/>

module Apiman {

    export var SwaggerUIContractService = _module.service('SwaggerUIContractService', function () {
        var key;

        var setXAPIKey = function (XAPIKey) {
            key = XAPIKey;
        };

        var getXAPIKey = function () {
            return key;
        };

        var removeXAPIKey = function () {
            key = "";
        };

        //return functions
        return {
            setXAPIKey: setXAPIKey,
            getXAPIKey: getXAPIKey,
            removeXAPIKey: removeXAPIKey
        }
    });
}