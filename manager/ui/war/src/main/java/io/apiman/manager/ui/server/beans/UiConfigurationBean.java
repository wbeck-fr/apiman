/*
 * Copyright 2014 JBoss Inc
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
package io.apiman.manager.ui.server.beans;


/**
 * Contains information about the UI.
 *
 * @author eric.wittmann@redhat.com
 */
public class UiConfigurationBean {

    private String header;
    private Boolean metrics;
    private String platform = "community"; //$NON-NLS-1$
    private String backToConsole;
    private Boolean developmentMode;
    private String keycloakEndpoint;
    private String devportalEndpoint;
    private String kibanaEndpoint;


    /**
     * Constructor.
     */
    public UiConfigurationBean() {
    }

    /**
     * @return the header
     */
    public String getHeader() {
        return header;
    }

    /**
     * @param header the header to set
     */
    public void setHeader(String header) {
        this.header = header;
    }

    /**
     * @return the metrics
     */
    public Boolean getMetrics() {
        return metrics;
    }

    /**
     * @param metrics the metrics to set
     */
    public void setMetrics(Boolean metrics) {
        this.metrics = metrics;
    }

    /**
     * @return the platform
     */
    public String getPlatform() {
        return platform;
    }

    /**
     * @param platform the platform to set
     */
    public void setPlatform(String platform) {
        this.platform = platform;
    }

    /**
     * @return the backToUrl
     */
    public String getBackToUrl() {
        return backToConsole;
    }

    /**
     * @param backToUrl the backToUrl to set
     */
    public void setBackToUrl(String backToUrl) {
        this.backToConsole = backToUrl;
    }

    /**
     * @return developmentMode the development mode
     */
    public Boolean getDevelopmentMode() {
        return developmentMode;
    }

    /**
     * @param developmentMode the development mode to set
     */
    public void setDevelopmentMode(Boolean developmentMode) {
        this.developmentMode = developmentMode;
    }

    /**
     * @return the keycloak endpoint
     */
    public String getKeycloakEndpoint() {
        return keycloakEndpoint;
    }

    /**
     * @param keycloakEndpoint the keycloak endpoint to set
     */
    public void setKeycloakEndpoint(String keycloakEndpoint) {
        this.keycloakEndpoint = keycloakEndpoint;
    }

    /**
     * @return the Developer Portal endpoint
     */
    public String getDevportalEndpoint() {
        return devportalEndpoint;
    }

    /**
     * @param devportalEndpoint the endpoint of the Developer Portal to set
     */
    public void setDevportalEndpoint(String devportalEndpoint) {
        this.devportalEndpoint = devportalEndpoint;
    }

    /**
     * @return the kibana endpoint
     */
    public String getKibanaEndpoint() {
        return kibanaEndpoint;
    }

    /**
     * @param kibanaEndpoint the kibana endpoint to set
     */
    public void setKibanaEndpoint(String kibanaEndpoint) {
        this.kibanaEndpoint = kibanaEndpoint;
    }
}
