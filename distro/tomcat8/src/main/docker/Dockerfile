FROM ${docker.base-image}

ENV ARTIFACT ${project.build.finalName}
ENV KEYCLOAK_VERSION ${docker.keycloak-version}

COPY maven/target/$ARTIFACT-overlay.zip $CATALINA_HOME
COPY maven/target/keycloak-config-files/apiman $CATALINA_HOME/webapps/apiman
COPY maven/target/keycloak-config-files/apimanui $CATALINA_HOME/webapps/apimanui
COPY maven/target/tomcat-config-files/index.jsp $CATALINA_HOME/webapps/ROOT/index.jsp
# Add and unzip plugins (version will not change)
ADD maven/target/apiman-plugins/apiman-plugins-repository-7.0.0-overlay.tgz /root/.m2

WORKDIR $CATALINA_HOME

RUN unzip -o $ARTIFACT-overlay.zip && rm $ARTIFACT-overlay.zip
RUN mv conf/server-docker.xml conf/server.xml

WORKDIR $CATALINA_HOME/webapps

# Remove files from image (ES, Gateway)
RUN rm apiman-es.war && rm apiman-gateway.war && rm apiman-gateway-api.war
#Remove tomcat stuff we not need
RUN rm -rf docs && rm -rf examples && rm -rf host-manager && rm -rf manager

RUN unzip apiman.war -n -d apiman && unzip apimanui.war -n -d apimanui

# Disable initial configuration per default
RUN touch /usr/local/tomcat/data/bootstrap/apiman-default-config.json.imported

# Get Keycloak Adapter
WORKDIR $CATALINA_HOME
RUN wget "https://downloads.jboss.org/keycloak/$KEYCLOAK_VERSION/adapters/keycloak-oidc/keycloak-tomcat8-adapter-dist-$KEYCLOAK_VERSION.zip"
RUN cd lib && unzip ../keycloak-tomcat8-adapter-dist-$KEYCLOAK_VERSION.zip && rm ../keycloak-tomcat8-adapter-dist-$KEYCLOAK_VERSION.zip
