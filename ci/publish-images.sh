#!/usr/bin/env bash

# ./publishImage.sh VERSION RELEASE

if [[ $2 = "release" ]]; then
    docker tag api-mgmt/keycloak:$1 gitlab.scheer-group.com:8080/api-mgmt/keycloak:$1
    docker tag api-mgmt/gateway:$1 gitlab.scheer-group.com:8080/api-mgmt/gateway:$1
    docker tag api-mgmt/ui:$1 gitlab.scheer-group.com:8080/api-mgmt/ui:$1
    docker push gitlab.scheer-group.com:8080/api-mgmt/keycloak:$1
    docker push gitlab.scheer-group.com:8080/api-mgmt/gateway:$1
    docker push gitlab.scheer-group.com:8080/api-mgmt/ui:$1
    docker image rm -f gitlab.scheer-group.com:8080/api-mgmt/keycloak:$1
    docker image rm -f gitlab.scheer-group.com:8080/api-mgmt/gateway:$1
    docker image rm -f gitlab.scheer-group.com:8080/api-mgmt/ui:$1
else
    docker tag api-mgmt/keycloak:$1 gitlab.scheer-group.com:8080/api-mgmt/keycloak:latest
    docker tag api-mgmt/gateway:$1 gitlab.scheer-group.com:8080/api-mgmt/gateway:latest
    docker tag api-mgmt/ui:$1 gitlab.scheer-group.com:8080/api-mgmt/ui:latest
    docker push gitlab.scheer-group.com:8080/api-mgmt/keycloak:latest
    docker push gitlab.scheer-group.com:8080/api-mgmt/gateway:latest
    docker push gitlab.scheer-group.com:8080/api-mgmt/ui:latest
fi

