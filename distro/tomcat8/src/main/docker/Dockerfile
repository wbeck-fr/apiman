FROM ${docker.base-image}

ENV ARTIFACT ${project.artifactId}-${project.version}
ENV KEYCLOAK_VERSION ${docker.keycloak-version}

COPY maven/target/$ARTIFACT-overlay.zip $CATALINA_HOME
COPY maven/target/keycloak-config-files/apiman $CATALINA_HOME/webapps/apiman
COPY maven/target/keycloak-config-files/apimanui $CATALINA_HOME/webapps/apimanui
COPY maven/target/tomcat-config-files/index.jsp $CATALINA_HOME/webapps/ROOT/index.jsp

WORKDIR $CATALINA_HOME

RUN unzip -o $ARTIFACT-overlay.zip && rm $ARTIFACT-overlay.zip
RUN mv conf/server-docker.xml conf/server.xml

WORKDIR $CATALINA_HOME/webapps

# Remove files from image (ES, Gateway)
RUN rm apiman-es.war && rm apiman-gateway.war && rm apiman-gateway-api.war
#Remove tomcat stuff we not need
RUN rm -rf docs && rm -rf examples && rm -rf host-manager && rm -rf manager

RUN unzip apiman.war -n -d apiman && unzip apimanui.war -n -d apimanui

# Add plugins (version will not change)
WORKDIR /root/.m2
RUN wget "http://ci.e2e.ch/job/Apiman-Plugins-Pipeline/job/e2e_release/lastSuccessfulBuild/artifact/repository/target/apiman-plugins-repository-7.0.0.zip"
RUN unzip apiman-plugins-repository-7.0.0.zip && rm apiman-plugins-repository-7.0.0.zip

# Get Keycloak Adapter
WORKDIR $CATALINA_HOME
RUN wget "https://downloads.jboss.org/keycloak/$KEYCLOAK_VERSION/adapters/keycloak-oidc/keycloak-tomcat8-adapter-dist-$KEYCLOAK_VERSION.zip"
RUN cd lib && unzip ../keycloak-tomcat8-adapter-dist-$KEYCLOAK_VERSION.zip && rm ../keycloak-tomcat8-adapter-dist-$KEYCLOAK_VERSION.zip