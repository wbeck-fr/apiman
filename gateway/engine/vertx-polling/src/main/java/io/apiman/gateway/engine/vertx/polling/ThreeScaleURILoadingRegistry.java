/*
 * Copyright 2017 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.apiman.gateway.engine.vertx.polling;

import io.apiman.gateway.engine.IEngineConfig;
import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.Client;
import io.apiman.gateway.engine.beans.Policy;
import io.apiman.gateway.engine.impl.InMemoryRegistry;
import io.apiman.gateway.engine.vertx.polling.fetchers.AccessTokenResourceFetcher;
import io.apiman.gateway.engine.vertx.polling.fetchers.threescale.beans.Content;
import io.apiman.gateway.engine.vertx.polling.fetchers.threescale.beans.ProxyConfigRoot;
import io.apiman.gateway.engine.vertx.polling.fetchers.threescale.beans.Service;
import io.apiman.gateway.engine.vertx.polling.fetchers.threescale.beans.ServicesRoot;
import io.apiman.gateway.platforms.vertx3.common.AsyncInitialize;
import io.apiman.gateway.platforms.vertx3.common.verticles.Json;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.impl.Arguments;
import io.vertx.core.json.DecodeException;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
* @author Marc Savy {@literal <marc@rhymewithgravy.com>}
*/
@SuppressWarnings("nls")
public class ThreeScaleURILoadingRegistry extends InMemoryRegistry implements AsyncInitialize {
    // Protected by DCL, use #getUriLoader
    private static volatile OneShotURILoader instance;
    private URI apiUri;
    private Vertx vertx;
    private Map<String, String> options;

    private String requireOpt(String key, String errorMsg) {
        Arguments.require(options.containsKey(key), errorMsg);
        return options.get(key);
    }

    public ThreeScaleURILoadingRegistry(Vertx vertx, IEngineConfig vxConfig, Map<String, String> options) {
        super();
        this.vertx = vertx;
        this.options = options;
        apiUri = URI.create(requireOpt("apiEndpoint", "apiEndpoint is required in configuration"));
    }

    public ThreeScaleURILoadingRegistry(Map<String, String> options) {
        this(Vertx.vertx(), null, options);
    }

    @Override
    public void initialize(IAsyncResultHandler<Void> resultHandler) {
        getURILoader(vertx, apiUri, options).subscribe(this, resultHandler::handle);
    }

    private OneShotURILoader getURILoader(Vertx vertx, URI uri, Map<String, String> options) {
        if (instance == null) {
            synchronized(ThreeScaleURILoadingRegistry.class) {
                if (instance == null) {
                    instance = new OneShotURILoader(vertx, uri, options);
                }
            }
        }
        return instance;
    }

    public static void reloadData(IAsyncHandler<Void> doneHandler) {
        instance.reload(doneHandler);
    }

    public static void reset() {
        synchronized(ThreeScaleURILoadingRegistry.class) {
            instance = null;
        }
    }

    @Override
    public void publishApi(Api api, IAsyncResultHandler<Void> handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void retireApi(Api api, IAsyncResultHandler<Void> handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void registerClient(Client client, IAsyncResultHandler<Void> handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unregisterClient(Client client, IAsyncResultHandler<Void> handler) {
        throw new UnsupportedOperationException();
    }

    protected void publishApiInternal(Api api, IAsyncResultHandler<Void> handler) {
        super.publishApi(api, handler);
    }

    protected void registerClientInternal(Client client, IAsyncResultHandler<Void> handler) {
        super.registerClient(client, handler);
    }

    private static final class OneShotURILoader {
        private Vertx vertx;
        private URI apiUri;
        private Map<String, String> config;
        private List<IAsyncResultHandler<Void>> failureHandlers = new ArrayList<>();
        private Deque<ThreeScaleURILoadingRegistry> awaiting = new ArrayDeque<>();
        private List<ThreeScaleURILoadingRegistry> allRegistries = new ArrayList<>(); // TODO for testing, perhaps can get rid of?
        private boolean dataProcessed = false;
        private List<Client> clients = new ArrayList<>();
        private List<Api> apis = new ArrayList<>();
        private Logger log = LoggerFactory.getLogger(OneShotURILoader.class);
        private IAsyncHandler<Void> reloadHandler;


        public OneShotURILoader(Vertx vertx, URI uri, Map<String, String> config) {
            this.config = config;
            this.vertx = vertx;
            this.apiUri = uri;
            fetchResource();
        }

        // Clear all registries and add back to processing queue.
        public synchronized void reload(IAsyncHandler<Void> reloadHandler) {
            this.reloadHandler = reloadHandler;
            awaiting.addAll(allRegistries);
            apis.clear();
            clients.clear();
            failureHandlers.clear();
            allRegistries.stream()
                .map(ThreeScaleURILoadingRegistry::getMap)
                .forEach(Map::clear);
            dataProcessed = false;
            // Load again from scratch.
            fetchResource();
        }

        private void fetchResource() {
            System.out.println("Fetching resources");
            getServicesRoot(servicesRoot -> {
                List<Service> services = servicesRoot.getServices();
                // Get all service IDs
                List<Long> sids = services.stream()
                        .map(service -> service.getService().getId())
                        .collect(Collectors.toList());
                // Get all configs.
                @SuppressWarnings("rawtypes")
                List<Future> futureList = sids.stream()
                    .map(this::getConfig)
                    .collect(Collectors.toList());

                CompositeFuture.all(futureList)
                    .setHandler(result -> {
                        if (result.succeeded()) {
                            processData();
                        } else {
                            failAll(result.cause());
                        }
                    });
            });

        }

        // https://ewittman-admin.3scale.net/admin/api/services/2555417735060/proxy/configs/production/latest.json
        // ?access_token=914e2f81d22b0c1baf62e75250d3daab9bec675318ecb555b8e39f91877ed5a8
        private List<ProxyConfigRoot> configs = new ArrayList<>();

        @SuppressWarnings("rawtypes")
        private Future getConfig(long id) {
            Future future = Future.future();
            String path = String.format("/admin/api/services/%d/proxy/configs/production/latest.json", id);
            new AccessTokenResourceFetcher(vertx, config, joinPath(path))
                .exceptionHandler(future::fail)
                .fetch(buffer -> {
                    if (buffer.length() > 0) {
                        ProxyConfigRoot pc = Json.decodeValue(buffer.toString(), ProxyConfigRoot.class);
                        log.debug("Received Proxy Config: {0}", pc);
                        configs.add(pc);
                    }
                    future.complete();
                });
            return future;
        }

        private void getServicesRoot(Handler<ServicesRoot> resultHandler) {
            new AccessTokenResourceFetcher(vertx, config, joinPath("/admin/api/services.json"))
                .exceptionHandler(this::failAll)
                .fetch(buffer -> {
                    ServicesRoot sr = Json.decodeValue(buffer.toString(), ServicesRoot.class);
                    System.out.println("Received buffer");
                    //log.debug("Received Services: {0}", sr);
                    resultHandler.handle(sr);
                });
        }

        // Bit messy, refactor
        private URI joinPath(String path) {
            try {
                return new URL(apiUri.toURL(), path).toURI();
            } catch (MalformedURLException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        private void processData() {
            if (configs.size() == 0) {
                log.warn("File loaded into registry was empty. No entities created.");
                return;
            }
            try {

                // Naive version initially.
                for (ProxyConfigRoot root : configs) {
                    // Reflects the remote data structure.
                    Content config = root.getProxyConfig().getContent();
                    Api api = new Api();
                    api.setEndpoint(config.getProxy().getEndpoint());
                    set3ScalePolicy(api, root);
                    api.setPublicAPI(true);

                    // API ID = service id (i think)
                    api.setApiId(config.getSystemName());
                    api.setOrganizationId("DEFAULT");
                    api.setEndpoint(config.getProxy().getApiBackend());
                    api.setEndpointContentType("text/json"); // don't think there is an equivalent of this in 3scale
                    api.setEndpointType("rest"); //don't think there is an equivalent of this in 3scale
                    api.setParsePayload(false); // can let user override this?
                    api.setPublicAPI(true); // is there an equivalent of this?
                    api.setVersion("DEFAULT"); // don't think this is relevant anymore

                    log.info("Processing - {0}: ", config);
                    log.info("Creating API - {0}: ", api);
                    apis.add(api);
                }

                dataProcessed = true;
                checkQueue();
            } catch (DecodeException e) {
                failAll(e);
            }
        }

        private void set3ScalePolicy(Api api, ProxyConfigRoot config) { // FIXME optimise
            //JsonObject json = new JsonObject().put("proxyConfig", new JsonObject(Json.encode(root.getProxyConfig())));
            Policy pol = new Policy();
            pol.setPolicyImpl("plugin:io.apiman.plugins:apiman-plugins-3scale-auth:1.2.10-SNAPSHOT:war/io.apiman.plugins.auth3scale.Auth3Scale"); // TODO get version? Hmm! Env?
            pol.setPolicyJsonConfig(Json.encode(config));
            api.getApiPolicies().add(pol);
        }

        public synchronized void subscribe(ThreeScaleURILoadingRegistry registry, IAsyncResultHandler<Void> failureHandler) {
            Objects.requireNonNull(registry, "registry must be non-null.");
            Objects.requireNonNull(failureHandler, "failure handler must be non-null.");
            failureHandlers.add(failureHandler);
            allRegistries.add(registry);
            awaiting.add(registry);
            vertx.runOnContext(action -> checkQueue());
        }

        private void checkQueue() {
            if (dataProcessed && awaiting.size()>0) {
                loadDataIntoRegistries();
            }
        }

        private void loadDataIntoRegistries() {
            ThreeScaleURILoadingRegistry reg = null;
            while ((reg = awaiting.poll()) != null) {
                log.debug("Loading data into registry {0}:", reg);
                for (Api api : apis) {
                    reg.publishApiInternal(api, handleAnyFailure());
                    log.debug("Publishing: {0} ", api);
                }
                for (Client client : clients) {
                    reg.registerClientInternal(client, handleAnyFailure());
                    log.debug("Registering: {0} ", client);
                }
            }
            if (reloadHandler != null)
                reloadHandler.handle((Void) null);
        }

        private IAsyncResultHandler<Void> handleAnyFailure() {
            return result -> {
                if (result.isError()) {
                    failAll(result.getError());
                    throw new RuntimeException(result.getError());
                }
            };
        }

        private void failAll(Throwable cause) {
            AsyncResultImpl<Void> failure = AsyncResultImpl.create(cause);
            failureHandlers.stream().forEach(failureHandler -> {
                vertx.runOnContext(run -> failureHandler.handle(failure));
            });
        }
    }

    public static void main(String[] args) {
//        Map<String, String> opts = new HashMap<>();
//        opts.put("apiEndpoint", "https://ewittman-admin.3scale.net/");
//        opts.put("accessToken", "914e2f81d22b0c1baf62e77250d3daab9bec675318ebb555b8e39f91877ed5a8");
//        ThreeScaleURILoadingRegistry reg = new ThreeScaleURILoadingRegistry(Vertx.vertx(), null, opts);
//        reg.initialize(res -> {
//            if (res.isError()) {
//                throw new RuntimeException(res.getError());
//            }
//        });

      String in =  "{\"proxy_config\":{\"id\":14,\"version\":1,\"environment\":\"production\",\"content\":{\"id\":2555417735624,\"account_id\":2445581528354,\"name\":\"Another Service\",\"oneline_description\":null,\"description\":\"Another service\",\"txt_api\":null,\"txt_support\":null,\"txt_features\":null,\"created_at\":\"2016-09-07T14:34:38Z\",\"updated_at\":\"2016-10-25T12:18:28Z\",\"logo_file_name\":null,\"logo_content_type\":null,\"logo_file_size\":null,\"state\":\"incomplete\",\"intentions_required\":false,\"draft_name\":\"\",\"infobar\":null,\"terms\":null,\"display_provider_keys\":false,\"tech_support_email\":null,\"admin_support_email\":null,\"credit_card_support_email\":null,\"buyers_manage_apps\":true,\"buyers_manage_keys\":true,\"custom_keys_enabled\":false,\"buyer_plan_change_permission\":\"none\",\"buyer_can_select_plan\":false,\"notification_settings\":null,\"default_application_plan_id\":2357355868190,\"default_service_plan_id\":2357355868189,\"buyer_can_see_log_requests\":false,\"default_end_user_plan_id\":null,\"end_user_registration_required\":true,\"tenant_id\":2445581528354,\"system_name\":\"anotherservice\",\"backend_version\":\"2\",\"mandatory_app_key\":true,\"buyer_key_regenerate_enabled\":true,\"support_email\":\"eric.wittmann@redhat.com\",\"referrer_filters_required\":false,\"deployment_option\":\"on_premise\",\"proxiable?\":true,\"backend_authentication_type\":\"service_token\",\"backend_authentication_value\":\"0cced6bfbe94e73ac8143ecf18253e2bddb3db97f18f0a55129e72403c9bba75\",\"proxy\":{\"id\":81928,\"tenant_id\":2445581528354,\"service_id\":2555417735624,\"endpoint\":\"\",\"deployed_at\":\"2016-10-25T12:18:36Z\",\"api_backend\":\"https://echo-api.3scale.net:443\",\"auth_app_key\":\"app_key\",\"auth_app_id\":\"app_id\",\"auth_user_key\":\"user_key_foobar\",\"credentials_location\":\"query\",\"error_auth_failed\":\"Authentication failed\",\"error_auth_missing\":\"Authentication parameters missing\",\"created_at\":\"2016-09-07T14:34:38Z\",\"updated_at\":\"2016-09-07T17:07:32Z\",\"error_status_auth_failed\":403,\"error_headers_auth_failed\":\"text/plain; charset=us-ascii\",\"error_status_auth_missing\":403,\"error_headers_auth_missing\":\"text/plain; charset=us-ascii\",\"error_no_match\":\"No Mapping Rule matched\",\"error_status_no_match\":404,\"error_headers_no_match\":\"text/plain; charset=us-ascii\",\"secret_token\":\"Shared_secret_sent_from_proxy_to_API_backend_f0f4cbff3f17df56\",\"hostname_rewrite\":\"\",\"oauth_login_url\":null,\"sandbox_endpoint\":\"https://anotherservice-2445581528354.staging.apicast.io:443\",\"api_test_path\":\"/\",\"api_test_success\":true,\"hostname_rewrite_for_sandbox\":\"echo-api.3scale.net\",\"endpoint_port\":80,\"valid?\":true,\"service_backend_version\":\"2\",\"hosts\":[\"anotherservice-2445581528354.staging.apicast.io\"],\"backend\":{\"endpoint\":\"https://su1.3scale.net\",\"host\":\"su1.3scale.net\"},\"proxy_rules\":[{\"id\":122619,\"proxy_id\":81928,\"http_method\":\"GET\",\"pattern\":\"/\",\"metric_id\":2555417956740,\"metric_system_name\":\"hits\",\"delta\":1,\"tenant_id\":2445581528354,\"created_at\":\"2016-09-07T14:34:38Z\",\"updated_at\":\"2016-09-07T14:34:38Z\",\"redirect_url\":null,\"parameters\":[],\"querystring_parameters\":{}}]}}}}\n";

      Json.decodeValue(in, ProxyConfigRoot.class);

    }

}
