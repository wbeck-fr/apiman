#!/usr/bin/bash

# If $TLS_ALLOWED_PROTOCOLS is set, this script will change the allowed TLS protocols and restart keycloak.
# The sleep is needed to wait till keycloak has started.

if [[ ! -z "$TLS_ALLOWED_PROTOCOLS" ]]
then

    # Split comma separated list into array by replacing ',' with ' '
    # https://stackoverflow.com/a/5257398
    PROTOCOLS_ARRAY=(${TLS_ALLOWED_PROTOCOLS//,/ })

    # Adapt string format for keycloak
    for protocol in "${PROTOCOLS_ARRAY[@]}"; do
        PROTOCOLS+="\"${protocol}\","
    done

    # Remove last element of the string, in this case ','
    PROTOCOLS=${PROTOCOLS%?}

    printf "\nKeycloak needs to be restarted after startup to change the configured TLS version ["$PROTOCOLS"]. This can take several minutes.\n"
    sleep 45 \
    && /opt/jboss/keycloak/bin/jboss-cli.sh --connect --commands='/subsystem=elytron/server-ssl-context=kcSSLContext:write-attribute(name=protocols, value=['$PROTOCOLS'])' \
    && /opt/jboss/keycloak/bin/jboss-cli.sh --connect --commands=':reload' &
fi

