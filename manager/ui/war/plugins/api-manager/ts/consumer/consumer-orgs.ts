/// <reference path="../apimanPlugin.ts"/>
module Apiman {

    export var ConsumerOrgsController = _module.controller("Apiman.ConsumerOrgsController",
        ['$q', '$location', '$scope', 'ApimanSvcs', 'PageLifecycle', 'Logger', 'CurrentUser',
        ($q, $location, $scope, ApimanSvcs, PageLifecycle, Logger, CurrentUser) => {
            var params = $location.search();
            if (params.q) {
                $scope.orgName = params.q;
            }

            // search in elasticseach
            $scope.searchOrg = function(value) {
                $location.search('q', value);
            };

            // filter at client side
            $scope.filterOrgs = function (searchText) {
                $scope.criteria = {
                    name: searchText
                };
            };
            
            var pageData = {
                orgs: $q(function(resolve, reject) {
                    if (params.q) {
                        var body:any = {};
                        body.filters = [];
                        body.filters.push( {"name": "name", "value": "*" + params.q + "*", "operator": "like"});
                        body.page = 1;
                        body.pageSize = 10000; // ES index.max_result_window
                        var searchStr = angular.toJson(body);
                        ApimanSvcs.save({ entityType: 'search', secondaryType: 'organizations' }, searchStr, function(result) { 
                            resolve(result.beans);
                        }, reject);
                    } else {
                        resolve([]);
                    }
                })
            };

            function loadAllEntries() {
                if ($scope.orgs.length == 0) {
                    $scope.searchOrg('*');
                }
            }

            PageLifecycle.loadPage('ConsumerOrgs', undefined, pageData, $scope, function() {
                PageLifecycle.setPageTitle('consumer-orgs');
                loadAllEntries();
                $scope.$applyAsync(function() {
                    angular.forEach($scope.orgs, function(org) {
                        org.isMember = CurrentUser.isMember(org.id);
                    });
                });
            });
        }]);

}
