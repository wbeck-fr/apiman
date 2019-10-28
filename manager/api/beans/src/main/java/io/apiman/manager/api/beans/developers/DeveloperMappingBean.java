package io.apiman.manager.api.beans.developers;

import java.io.Serializable;

/**
 * Models the mapping data for a developer
 */
public class DeveloperMappingBean implements Serializable {

    private static final long serialVersionUID = -5334196591430185705L;

    private String clientId;
    private String organizationId;

    /**
     * Constructor
     */
    public DeveloperMappingBean() {
    }

    /**
     * Get the client id
     * @return the client id
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Set the client id
     * @param clientId the client id
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * Get the organiztaion id
     * @return the organization id
     */
    public String getOrganizationId() {
        return organizationId;
    }

    /**
     * Set the organization id
     * @param organizationId the organization id
     */
    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }
}

