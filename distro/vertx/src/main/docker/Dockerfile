FROM ${docker.base-image}

ENV ARTIFACT ${project.artifactId}-${project.version}

# Add and unzip plugins (version will not change)
ADD maven/target/apiman-plugins/apiman-plugins-repository-7.0.0-overlay.tgz /root/.m2
ADD maven/target/$ARTIFACT.tgz /usr/src/apiman

WORKDIR /usr/src/apiman
# Remove version number, easier to mount config
RUN mv $ARTIFACT ${project.artifactId}

WORKDIR /usr/src/apiman/${project.artifactId}
CMD ["./apiman-gateway.sh", "--conf", "configs/conf-production.json"]
