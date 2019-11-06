package io.apiman.manager.api.beans.developers;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.Set;

/**
 * Models the data that are used to create or update a developer
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NewDeveloperBean implements Serializable {

    private static final long serialVersionUID = 5197909289931057303L;
    private String name;
    private Set<DeveloperMappingBean> clients;

    /**
     * Constructor
     */
    public NewDeveloperBean() {
    }

    /**
     * Get the clients
     *
     * @return list of the clients
     */
    public Set<DeveloperMappingBean> getClients() {
        return clients;
    }

    /**
     * Set the clients
     *
     * @param clients the list of clients
     */
    public void setClients(Set<DeveloperMappingBean> clients) {
        this.clients = clients;
    }

    /**
     * Get the name
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "NewDeveloperBean [name=" + name + ",clients=" + clients + "]";
    }
}
