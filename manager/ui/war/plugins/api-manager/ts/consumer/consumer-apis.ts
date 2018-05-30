/// <reference path="../apimanPlugin.ts"/>
module Apiman {

    export var ConsumerApisController = _module.controller("Apiman.ConsumerApisController",
        ['$q', '$location', '$scope', 'ApimanSvcs', 'PageLifecycle', 'Logger',
        ($q, $location, $scope, ApimanSvcs, PageLifecycle, Logger) => {
            var params = $location.search();
            if (params.q) {
                $scope.apiName = params.q;
            }

            $scope.searchSvcs = function(value) {
                $location.search('q', value);
            };
            
            var pageData = {
                apis: $q(function(resolve, reject) {
                    if (params.q) {
                        var body:any = {};
                        body.filters = [];
                        body.filters.push( {"name": "name", "value": "*" + params.q + "*", "operator": "like"});
                        var searchStr = angular.toJson(body);
                        
                        ApimanSvcs.save({ entityType: 'search', secondaryType: 'apis' }, searchStr, function(reply) {
                            resolve(reply.beans);
                        }, reject);
                    } else {
                        resolve([]);
                    }
                })
            };

            function loadAllEntries() {
                if ($scope.apis.length == 0) {
                   $scope.searchSvcs('*')
                }
                $('#apiman-search').val(function (index, value) {
                    return value.replace('*','');
                });
            }

            PageLifecycle.loadPage('ConsumerApis', undefined, pageData, $scope, function() {
                PageLifecycle.setPageTitle('consumer-apis');
                loadAllEntries()
                $scope.$applyAsync(function() {
                    $('#apiman-search').focus();
                });
            });
        }]);

}
