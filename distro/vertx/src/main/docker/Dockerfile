FROM ${docker.base-image}

ENV ARTIFACT ${project.artifactId}-${project.version}

COPY maven/target /usr/src/apiman

WORKDIR /usr/src/apiman

RUN unzip $ARTIFACT.zip && rm $ARTIFACT.zip

# Remove version number, easier to mount config
RUN mv $ARTIFACT ${project.artifactId}

# Add plugins (version will not change)
WORKDIR /root/.m2
RUN wget "http://ci.e2e.ch/job/Apiman-Plugins-Pipeline/job/e2e_release/lastSuccessfulBuild/artifact/repository/target/apiman-plugins-repository-7.0.0.zip"
RUN unzip apiman-plugins-repository-7.0.0.zip && rm apiman-plugins-repository-7.0.0.zip

EXPOSE 8081 8082

WORKDIR /usr/src/apiman/${project.artifactId}
CMD ["./apiman-gateway.sh", "--conf", "configs/conf-production.json"]