FROM ${docker.base-image}

# Add our custom theme
COPY maven/target/resources/apiman /opt/jboss/keycloak/themes/apiman/
COPY maven/target/resources/keycloak-default-realm.json /opt/api-mgmt/keycloak-default-realm.json
# Add script for self-signed certificates
COPY maven/target/resources/create-self-signed-certificates.sh /opt/api-mgmt/create-self-signed-certificates.sh
COPY maven/target/resources/set-allowed-ssl-tls-protocols.sh /opt/jboss/startup-scripts/set-allowed-ssl-tls-protocols.sh
