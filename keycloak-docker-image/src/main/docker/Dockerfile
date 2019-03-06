FROM ${docker.base-image}

# Add our custom theme
COPY maven/target/resources/apiman /opt/jboss/keycloak/themes/apiman/
COPY maven/target/resources/realm.json /opt/api-mgmt/realm.json
# Add script for self-signed certificates
COPY maven/target/resources/create-self-signed-certificates.sh /opt/api-mgmt/create-self-signed-certificates.sh