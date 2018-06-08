/// <reference path="../../includes.ts"/>
module ApimanPageLifecycle {

    export var pageTitles = {
        "page.title.admin-gateways": "API Management - Admin - Gateways",
        "page.title.admin-plugins": "API Management - Admin - Plugins",
        "page.title.admin-roles": "API Management - Admin - Roles",
        "page.title.admin-policyDefs": "API Management - Admin - Policy Definitions",
        "page.title.admin-export": "API Management - Admin - Export/Import",
        "page.title.api-catalog": "API Management - API Catalog",
        "page.title.api-catalog-def": "API Management - API Definition",
        "page.title.client-activity": "API Management - {0} (Change Log)",
        "page.title.client-apis": "API Management - {0} (APIs)",
        "page.title.client-contracts": "API Management - {0} (Contracts)",
        "page.title.client-metrics": "API Management - {0} (Metrics)",
        "page.title.client-overview": "API Management - {0} (Overview)",
        "page.title.client-policies": "API Management - {0} (Policies)",
        "page.title.consumer-org": "API Management - Organization {0}",
        "page.title.consumer-orgs": "API Management - Organizations",
        "page.title.consumer-api": "API Management - API {0}",
        "page.title.consumer-api-def": "API Management - API {0} - Definition",
        "page.title.consumer-apis": "API Management - APIs",
        "page.title.dashboard": "API Management - Home",
        "page.title.about": "API Management - About",
        "page.title.edit-gateway": "API Management - Edit Gateway",
        "page.title.edit-policy": "API Management - Edit Policy",
        "page.title.edit-policyDef": "API Management - Edit Policy Definition",
        "page.title.edit-role": "API Management - Edit Role",
        "page.title.import-policyDefs": "API Management - Import Policy Definition(s)",
        "page.title.import-apis": "API Management - API Catalog Import",
        "page.title.new-client": "API Management - New Client",
        "page.title.new-client-version": "API Management - New Client Version",
        "page.title.new-contract": "API Management - New Contract",
        "page.title.new-gateway": "API Management - New Gateway",
        "page.title.new-member": "API Management - Add Member",
        "page.title.new-org": "API Management - New Organization",
        "page.title.new-plan": "API Management - New Plan",
        "page.title.new-plan-version": "API Management - New Plan Version",
        "page.title.new-plugin": "API Management - Add Plugin",
        "page.title.new-policy": "API Management - Add Policy",
        "page.title.new-role": "API Management - New Role",
        "page.title.new-api": "API Management - New API",
        "page.title.new-api-version": "API Management - New API Version",
        "page.title.org-activity": "API Management - {0} (Change Log)",
        "page.title.org-clients": "API Management - {0} (Client)",
        "page.title.org-manage-members": "API Management - {0} (Manage Members)",
        "page.title.org-members": "API Management - {0} (Members)",
        "page.title.org-plans": "API Management - {0} (Plans)",
        "page.title.org-apis": "API Management - {0} (APIs)",
        "page.title.plan-activity": "API Management - {0} (Change Log)",
        "page.title.plan-overview": "API Management - {0} (Overview)",
        "page.title.plan-policies": "API Management - {0} (Policies)",
        "page.title.plugin-details": "API Management - Plugin Details",
        "page.title.policy-defs": "API Management - Admin - Policy Definitions",
        "page.title.api-activity": "API Management - {0} (Change Log)",
        "page.title.api-contracts": "API Management - {0} (Contracts)",
        "page.title.api-endpoint": "API Management - {0} (Endpoint)",
        "page.title.api-metrics": "API Management - {0} (Metrics)",
        "page.title.api-impl": "API Management - {0} (Implementation)",
        "page.title.api-def": "API Management - {0} (Definition)",
        "page.title.api-overview": "API Management - {0} (Overview)",
        "page.title.api-plans": "API Management - {0} (Plans)",
        "page.title.api-policies": "API Management - {0} (Policies)",
        "page.title.user-activity": "API Management - {0} (Change Log)",
        "page.title.user-clients": "API Management - {0} (Clients)",
        "page.title.user-orgs": "API Management - {0} (Organizations)",
        "page.title.user-profile": "API Management - User Profile",
        "page.title.user-apis": "API Management - {0} (APIs)",
        "page.title.error": "API Management - {0} Error",
    };
    
    var formatMessage = function(theArgs) {
        var now = new Date();
        var msg = theArgs[0];
        if (theArgs.length > 1) {
            for (var i = 1; i < theArgs.length; i++) {
                msg = msg.replace('{'+(i-1)+'}', theArgs[i]);
            }
        }
        return msg;
    };

    export var _module = angular.module("ApimanPageLifecycle", []);

    export var PageLifecycle = _module.factory('PageLifecycle', 
        ['$q', '$timeout', 'Logger', '$rootScope', '$location', 'CurrentUserSvcs', 'Configuration', 'TranslationSvc', '$window', 'CurrentUser',
        ($q, $timeout, Logger, $rootScope, $location, CurrentUserSvcs, Configuration, TranslationSvc, $window, CurrentUser) => {
            var header = 'community';
            if (Configuration.ui && Configuration.ui.header) {
                header = Configuration.ui.header;
            }
            if (header == 'apiman') {
                header = 'community';
            }
            $rootScope.headerInclude = 'plugins/api-manager/html/headers/' + header + '.include';
            console.log('Using header: ' + $rootScope.headerInclude);

            $rootScope.developmentMode = Configuration.ui && Configuration.ui.developmentMode;
            console.log('Development mode: ' + $rootScope.developmentMode);

            let redirectWrongPermission = function () {
                Logger.info('Detected a 404 error.');
                $location.url(Apiman.pluginName + '/errors/404').replace();
                return;
            };

            var processCurrentUser = function(currentUser) {
                $rootScope.currentUser = currentUser;
                var permissions = {};
                var memberships = {};
                if (currentUser.permissions) {
                    for (var i = 0; i < currentUser.permissions.length; i++) {
                        var perm = currentUser.permissions[i];
                        var permid = perm.organizationId + '||' + perm.name;
                        permissions[permid] = true;
                        memberships[perm.organizationId] = true;
                    }
                }
                Logger.info('Updating permissions now {0}', permissions);
                $rootScope.permissions = permissions;
                $rootScope.memberships = memberships;
            };
            var handleError = function(error) {
                $rootScope.pageState = 'error';
                $rootScope.pageError = error;
                if (error.status == 400) {
                    Logger.info('Detected an error {0}, redirecting to 400.', error.status);
                    $location.url(Apiman.pluginName + '/errors/400').replace();
                } else if (error.status == 401) {
                    Logger.info('Detected an error 401, reloading the page.');
                    $window.location.reload();
                } else if (error.status == 403) {
                    Logger.info('Detected an error {0}, redirecting to 403.', error.status);
                    $location.url(Apiman.pluginName + '/errors/403').replace();
                } else if (error.status == 404) {
                    Logger.info('Detected an error {0}, redirecting to 404.', error.status);
                    $location.url(Apiman.pluginName + '/errors/404').replace();
                } else if (error.status == 409) {
                    Logger.info('Detected an error {0}, redirecting to 409.', error.status);
                    var errorUri = '409';
                    Logger.info('=====> {0}', error);
                    Logger.info('=====> error code: {0}', error.data.errorCode);
                    if (error.data.errorCode && error.data.errorCode == 8002) {
                        errorUri = '409-8002';
                    }
                    $location.url(Apiman.pluginName + '/errors/' + errorUri).replace();
                } else if (error.status == 0) {
                    Logger.info('Detected an error {0}, redirecting to CORS error page.', error.status);
                    $location.url(Apiman.pluginName + '/errors/invalid_server').replace();
                } else {
                    // TODO: if the error data starts with <html> then redirect to a more generic html-into-div based error page
                    Logger.info('Detected an error {0}, redirecting to 500.', error.status);
                    $location.url(Apiman.pluginName + '/errors/500').replace();
                }
            };
            return {
                setPageTitle: function(titleKey, params) {
                    var key = 'page.title.' + titleKey;
                    var pattern = pageTitles[key];
                    pattern = TranslationSvc.translate(key, pattern);
                    if (pattern) {
                        var args = [];
                        args.push(pattern);
                        args = args.concat(params);
                        var title = formatMessage(args);
                        document.title = title;
                    } else {
                        document.title = pattern;
                    }
                },
                handleError: handleError,
                forwardTo: function() {
                    var path = '/' + Apiman.pluginName + formatMessage(arguments);
                    Logger.info('Forwarding to page {0}', path);
                    $location.url(path).replace();
                },
                redirectTo: function() {
                    var path = '/' + Apiman.pluginName + formatMessage(arguments);
                    Logger.info('Redirecting to page {0}', path);
                    $location.url(path);
                },
                loadPage: function(pageName, requiredPermission, pageData, $scope, handler) {
                    Logger.log("|{0}| >> Loading page.", pageName);
                    $rootScope.pageState = 'loading';
                    $rootScope.isDirty = false;

                    // Every page gets the current user.
                    var allData = undefined;
                    var commonData = {
                        currentUser: $q(function(resolve, reject) {
                            if ($rootScope.currentUser) {
                                Logger.log("|{0}| >> Using cached current user from $rootScope.", pageName);
                                resolve($rootScope.currentUser);
                            } else {
                                CurrentUserSvcs.get({ what: 'info' }, function(currentUser) {
                                    processCurrentUser(currentUser);
                                    resolve(currentUser);
                                }, reject);
                            }
                        })
                    };

                    // If some additional page data is requested, merge it into the common data
                    if (pageData) {
                        allData = angular.extend({}, commonData, pageData);
                    } else {
                        allData = commonData;
                    }

                    // Now resolve the data as a promise (wait for all data packets to be fetched)
                    var promise = $q.all(allData);
                    promise.then(function(data) {
                        // Make sure the user has permission to view this page.
                        if (requiredPermission == "development"){
                            if ($rootScope.developmentMode != true){
                                redirectWrongPermission();
                            }
                        }

                        if ( (requiredPermission && requiredPermission == 'orgView' && !CurrentUser.isMember($scope.organizationId)) ||
                             ( requiredPermission && requiredPermission != 'orgView' && !CurrentUser.hasPermission($scope.organizationId, requiredPermission)) )
                        {
                            redirectWrongPermission();
                        }
                        // Now process all the data packets and bind them to the $scope.
                        var count = 0;
                        angular.forEach(data, function(value, key) {
                            Logger.debug("|{0}| >> Binding {1} to $scope.", pageName, key);
                            this[key] = value;
                            count++;
                        }, $scope);
                        
                        $timeout(function() {
                            $rootScope.pageState = 'loaded';
                            $rootScope.isAdmin = CurrentUser.getCurrentUser().admin;
                            Logger.log("|{0}| >> Page successfully loaded: {1} data packets loaded", pageName, count);
                            if (handler) {
                                $timeout(function() {
                                    Logger.log("|{0}| >> Calling Page onLoaded handler", pageName);
                                    handler();
                                }, 20);
                            }
                        }, 50);
                    }, function(reason) {
                        Logger.error("|{0}| >> Page load failed: {1}", pageName, reason);
                        handleError(reason);
                    });
                },
                loadErrorPage: function(pageName, $scope, handler) {
                    Logger.log("|{0}| >> Loading error page.", pageName);
                    $rootScope.pageState = 'loading';

                    // Nothing to do asynchronously for the error pages!
                    $rootScope.pageState = 'loaded';
                    if (handler) {
                        handler();
                    }
                    Logger.log("|{0}| >> Error page successfully loaded", pageName);
                }
            }
        }]);

}