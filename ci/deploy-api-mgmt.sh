#!/bin/bash

#$1 -> Installation Path
#$2 -> version nr
#$3 -> git commit short hash
#$4 -> build nr
#$5 -> branch name

# taken from https://gist.github.com/cdown/1163649
urlencode() {
    # urlencode <string>
    old_lc_collate=$LC_COLLATE
    LC_COLLATE=C

    local length="${#1}"
    for (( i = 0; i < length; i++ )); do
        local c="${1:i:1}"
        case $c in
            [a-zA-Z0-9.~_-]) printf "$c" ;;
            *) printf '%%%02X' "'$c" ;;
        esac
    done

    LC_COLLATE=$old_lc_collate
}

# URL encode branch name - jenkins url is encoded twice
BRANCH_NAME=$(urlencode $(urlencode $5))

#navigate to Apimgmt installation
cd $1

#get docker-compose file from zip
wget -O /tmp/apiman.zip http://ci.e2e.ch/job/Apiman-Pipeline/job/$BRANCH_NAME/$4/artifact/setups/target/api-mgmt-$2-$3.zip
docker-compose down
unzip -p /tmp/apiman.zip api-mgmt/single-host-setup/docker-compose.yml > docker-compose.yml

#replace version number to latest version
sed -i "s/${2}/latest/g" docker-compose.yml
#replace docker image with url from nexus
sed -i "s/api-mgmt\/gateway/gitlab.scheer-group.com:8080\/api-mgmt\/gateway/g" docker-compose.yml
sed -i "s/api-mgmt\/keycloak/gitlab.scheer-group.com:8080\/api-mgmt\/keycloak/g" docker-compose.yml
sed -i "s/api-mgmt\/ui/gitlab.scheer-group.com:8080\/api-mgmt\/ui/g" docker-compose.yml

#pull new docker images and start docker containers
docker-compose pull
docker-compose up -d
