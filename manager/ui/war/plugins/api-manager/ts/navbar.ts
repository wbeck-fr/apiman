/// <reference path="apimanPlugin.ts"/>
module Apiman {

    export var NavbarController = _module.controller("Apiman.NavbarController",
        ['$scope', 'Logger', 'Configuration', ($scope, Logger, Configuration) => {
            Logger.log("Current user is {0}.", Configuration.user.username);
            $scope.username = Configuration.user.username;
            $scope.logoutUrl = Configuration.apiman.logoutUrl;
            $scope.goBack = function () {
                Logger.info('Returning to parent UI: {0}', Configuration.ui.backToConsole);
                window.location.href = Configuration.ui.backToConsole;
            };

            angular.element(document).ready(function () {
                // Make header not scrollable
                angular.element('html').addClass('layout-pf layout-pf-fixed');

                // Add class "active" based on current href/url
                let path = window.location.pathname;

                // Match dev server and tomcat
                path = path.replace(/^\/apimanui\/|^\/|\/$/g, "");
                path = decodeURIComponent(path);

                angular.forEach(angular.element(".list-group-item a"), function (value) {
                    let item = angular.element(value);
                    let href = item.attr("href");
                    if (path === href) {
                        item.closest('li').addClass('active');
                        item.closest('.secondary-nav-item-pf').addClass('active');
                    }
                })
            });
        }]);
}
