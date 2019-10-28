package io.apiman.manager.api.beans.developers;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Models a developer
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeveloperBean implements Serializable {

    private static final long serialVersionUID = 7127400624541487145L;

    private String id;
    private String name;
    private Set<DeveloperMappingBean> clients = new LinkedHashSet<>();

    /**
     * Constructor
     */
    public DeveloperBean() {
    }

    /**
     * Get a list of clients
     *
     * @return The list of clients
     */
    public Set<DeveloperMappingBean> getClients() {
        return clients;
    }

    /**
     * Set a list of clients
     *
     * @param clients The list of clients
     */
    public void setClients(Set<DeveloperMappingBean> clients) {
        this.clients = clients;
    }

    /**
     * Get the id
     *
     * @return The id
     */
    public String getId() {
        return id;
    }

    /**
     * Set the id
     *
     * @param id The id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get the name
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name
     *
     * @param name The name
     */
    public void setName(String name) {
        this.name = name;
    }
}
