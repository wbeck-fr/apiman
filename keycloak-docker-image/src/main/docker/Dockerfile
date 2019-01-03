FROM ${docker.base-image}

# Add our custom theme
COPY maven/target/resources/apiman /opt/jboss/keycloak/themes/apiman/
COPY maven/target/resources/realm.json /tmp/realm.json