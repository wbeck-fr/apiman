/// <reference path="apimanPlugin.ts"/>
module Apiman {

    export var DashController = _module.controller("Apiman.AboutController",
        ['$scope', 'PageLifecycle', 'CurrentUser', 'Configuration',
        ($scope, PageLifecycle, CurrentUser, Configuration) => {
            PageLifecycle.loadPage('About', undefined, undefined, $scope, function() {
                $scope.site = "https://e2ebridge.com";
                $scope.userGuide = "https://doc.scheer-group.com/API+Management+Guide";
                // not used for now
                // $scope.tutorials = "http://www.apiman.io/latest/tutorials.html";
                $scope.version = Configuration.apiman.version;
                $scope.builtOn = Configuration.apiman.builtOn;
                $scope.apiEndpoint = Configuration.api.endpoint;
                PageLifecycle.setPageTitle('about');
            });
        }]);

}
