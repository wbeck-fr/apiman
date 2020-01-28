#!/usr/bin/env groovy

@Library('jenkins-jira-integration@dev') _

pipeline {
    agent {
        node {
            label 'cscabbia'
            customWorkspace "workspace/Apiman-Pipeline"
        }
    }

    environment {
        JAVA_HOME = '/usr/lib/jvm/jdk1.8.0_191'
        PROJECT_VERSION = readMavenPom().getVersion()
        // Snippet taken from https://issues.jenkins-ci.org/browse/JENKINS-44449
        GIT_COMMIT_SHORT = sh(
                script: "printf \$(git rev-parse --short=7 ${GIT_COMMIT})",
                returnStdout: true
        )
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10', artifactNumToKeepStr: '1'))
        disableConcurrentBuilds()
        lock('ApiMgmt-Build')
    }

    stages {
        stage('Clean') {
            steps {
                sh 'git clean -xdf'
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean install -DskipTests'
            }
        }

        stage('Build Test Image') {
            steps {
                script {
                    sh 'docker build -f ./ci/maven-docker.dockerfile -t maven-docker:latest .'
                }
            }
        }

        stage('Tests') {
            parallel {
                stage('default'){
                    environment {
                        JAVA_HOME = '/usr/local/openjdk-8'
                    }
                    agent {
                        // https://github.com/carlossg/docker-maven#running-as-non-root
                        docker {
                            image 'maven-docker:latest'
                            args '--group-add docker -v /var/run/docker.sock:/var/run/docker.sock -v $HOME/.m2:/var/maven/.m2 --tmpfs /.cache -e MAVEN_CONFIG=/var/maven/.m2 -e MAVEN_OPTS="-Duser.home=/var/maven"'
                            label 'docker'
                            customWorkspace "workspace/Apiman-Pipeline-Tests/default"
                        }
                    }
                    steps {
                        retry(2) {
                            sh 'git clean -xdf'
                            sh 'mvn clean test'
                        }
                    }
                    post {
                        always {
                            junit '**/target/surefire-reports/*.xml'
                        }
                    }
                }

                stage('servlet-es/es'){
                    environment {
                        JAVA_HOME = '/usr/local/openjdk-8'
                    }
                    agent {
                        // https://github.com/carlossg/docker-maven#running-as-non-root
                        docker {
                            image 'maven-docker:latest'
                            args '--group-add docker -v /var/run/docker.sock:/var/run/docker.sock -v $HOME/.m2:/var/maven/.m2 --tmpfs /.cache -e MAVEN_CONFIG=/var/maven/.m2 -e MAVEN_OPTS="-Duser.home=/var/maven"'
                            label 'docker'
                            customWorkspace "workspace/Apiman-Pipeline-Tests/servlet-es"
                        }
                    }
                    steps {
                        retry(2) {
                            sh 'git clean -xdf'
                            sh 'mvn clean test -Dapiman-test.type=es -Dapiman.gateway-test.config=servlet-es'
                        }
                    }
                    post {
                        always {
                            junit '**/target/surefire-reports/*.xml'
                        }
                    }
                }

                stage('vertx3-mem'){
                    environment {
                        JAVA_HOME = '/usr/local/openjdk-8'
                    }
                    agent {
                        // https://github.com/carlossg/docker-maven#running-as-non-root
                        docker {
                            image 'maven-docker:latest'
                            args '--group-add docker -v /var/run/docker.sock:/var/run/docker.sock -v $HOME/.m2:/var/maven/.m2 --tmpfs /.cache -e MAVEN_CONFIG=/var/maven/.m2 -e MAVEN_OPTS="-Duser.home=/var/maven"'
                            label 'docker'
                            customWorkspace "workspace/Apiman-Pipeline-Tests/vertx3-mem"
                        }
                    }
                    steps {
                        retry(2) {
                            sh 'git clean -xdf'
                            sh 'mvn -pl gateway/test clean test -Dapiman.gateway-test.config=vertx3-mem'
                        }
                    }
                    post {
                        always {
                            junit '**/target/surefire-reports/*.xml'
                        }
                    }
                }

                stage('vertx3-file'){
                    environment {
                        JAVA_HOME = '/usr/local/openjdk-8'
                    }
                    agent {
                        // https://github.com/carlossg/docker-maven#running-as-non-root
                        docker {
                            image 'maven-docker:latest'
                            args '--group-add docker -v /var/run/docker.sock:/var/run/docker.sock -v $HOME/.m2:/var/maven/.m2 --tmpfs /.cache -e MAVEN_CONFIG=/var/maven/.m2 -e MAVEN_OPTS="-Duser.home=/var/maven"'
                            label 'docker'
                            customWorkspace "workspace/Apiman-Pipeline-Tests/vertx3-file"
                        }
                    }
                    steps {
                        retry(2) {
                            sh 'git clean -xdf'
                            sh 'mvn -pl gateway/test clean test -Dapiman.gateway-test.config=vertx3-file'
                        }
                    }
                    post {
                        always {
                            junit '**/target/surefire-reports/*.xml'
                        }
                    }
                }

                stage('amg-1'){
                    environment {
                        JAVA_HOME = '/usr/local/openjdk-8'
                    }
                    agent {
                        // https://github.com/carlossg/docker-maven#running-as-non-root
                        docker {
                            image 'maven-docker:latest'
                            args '--group-add docker -v /var/run/docker.sock:/var/run/docker.sock -v $HOME/.m2:/var/maven/.m2 --tmpfs /.cache -e MAVEN_CONFIG=/var/maven/.m2 -e MAVEN_OPTS="-Duser.home=/var/maven"'
                            label 'docker'
                            customWorkspace "workspace/Apiman-Pipeline-Tests/amg-1"
                        }
                    }
                    steps {
                        retry(2) {
                            sh 'git clean -xdf'
                            sh 'mvn -pl gateway/test clean test -Dapiman.gateway-test.config=amg-1'
                        }
                    }
                    post {
                        always {
                            junit '**/target/surefire-reports/*.xml'
                        }
                    }
                }

                stage('vertx3-es'){
                    environment {
                        JAVA_HOME = '/usr/local/openjdk-8'
                    }
                    agent {
                        // https://github.com/carlossg/docker-maven#running-as-non-root
                        docker {
                            image 'maven-docker:latest'
                            args '--group-add docker -v /var/run/docker.sock:/var/run/docker.sock -v $HOME/.m2:/var/maven/.m2 --tmpfs /.cache -e MAVEN_CONFIG=/var/maven/.m2 -e MAVEN_OPTS="-Duser.home=/var/maven"'
                            label 'docker'
                            customWorkspace "workspace/Apiman-Pipeline-Tests/vertx3-es"
                        }
                    }
                    steps {
                        retry(2) {
                            sh 'git clean -xdf'
                            sh """
                                docker run -d -p 19250:9200 -p 9300:9300 -e "discovery.type=single-node" --name=elasticsearch elasticsearch:5.6.16
                                sleep 15
                                mvn -pl gateway/test clean test -Dapiman.gateway-test.config=vertx3-es
                            """
                        }
                    }
                    post {
                        always {
                            sh 'docker stop elasticsearch && docker rm elasticsearch'
                            junit '**/target/surefire-reports/*.xml'
                        }
                    }
                }
            }
        }

        stage('Trigger Devportal/Plugins') {
            parallel {
                stage('Release Build') {
                    when {
                        anyOf {
                            branch '**/e2e_release'
                        }
                    }
                    steps {
                        build job: '../Api-Mgmt-Dev-Portal/e2e_release', wait: false
                        build job: '../Apiman-Plugins-Pipeline/e2e_release', wait: true
                    }
                }

                stage('Master Build') {
                    when {
                        anyOf {
                            branch '**/e2e_master'
                        }
                    }
                    steps {
                        build job: '../Api-Mgmt-Dev-Portal/e2e_master', wait: false
                        build job: '../Apiman-Plugins-Pipeline/e2e_master', wait: true
                    }
                }
            }
        }

        stage('Build docker images') {
            steps {
                sh """
                    mvn clean package docker:build -P docker -pl !distro/wildfly10,!distro/wildfly11,!distro/eap7 -DskipTests
                    docker run --rm -v /var/run/docker.sock:/var/run/docker.sock rassie/docker-squash -t api-mgmt/ui:${PROJECT_VERSION} api-mgmt/ui:${PROJECT_VERSION}
                    docker run --rm -v /var/run/docker.sock:/var/run/docker.sock rassie/docker-squash -t api-mgmt/gateway:${PROJECT_VERSION} api-mgmt/gateway:${PROJECT_VERSION}
                    docker run --rm -v /var/run/docker.sock:/var/run/docker.sock rassie/docker-squash -t api-mgmt/keycloak:${PROJECT_VERSION} api-mgmt/keycloak:${PROJECT_VERSION}
                    docker image save api-mgmt/ui:${PROJECT_VERSION} -o api-mgmt-ui-${PROJECT_VERSION}-overlay.tar
                    docker image save api-mgmt/gateway:${PROJECT_VERSION} -o api-mgmt-gateway-${PROJECT_VERSION}-overlay.tar
                    docker image save api-mgmt/keycloak:${PROJECT_VERSION} -o api-mgmt-keycloak-${PROJECT_VERSION}-overlay.tar
                """
            }
        }
        stage('Archive builds') {
            when {
                not {
                    branch '**/e2e_release'
                }
            }
            steps {
                script {
                    dir('distro/tomcat8/target/') {
                        sh "rename.ul overlay ${GIT_COMMIT_SHORT} api-mgmt-tomcat*.zip"
                    }
                    dir('setups/target/') {
                        sh "rename.ul overlay ${GIT_COMMIT_SHORT} api-mgmt*.zip"
                    }
                    sh "rename.ul overlay ${GIT_COMMIT_SHORT} *.tar"
                }
                archiveArtifacts artifacts: 'distro/tomcat8/target/api-mgmt-tomcat*.zip'
                archiveArtifacts artifacts: 'setups/target/api-mgmt*.zip'
                archiveArtifacts artifacts: '*.tar'
            }
        }
        stage('Archive release builds') {
            when {
                anyOf {
                    branch '**/e2e_release'
                }
            }
            steps {
                dir('distro/tomcat8/target/') {
                    sh 'rename.ul -- "-overlay" "" api-mgmt-tomcat*.zip'
                }
                dir('setups/target/') {
                    sh 'rename.ul -- "-overlay" "" api-mgmt*.zip'
                }
                sh 'rename.ul -- "-overlay" "" *.tar'
                archiveArtifacts artifacts: 'distro/tomcat8/target/api-mgmt-tomcat*.zip'
                archiveArtifacts artifacts: 'setups/target/api-mgmt*.zip'
                archiveArtifacts artifacts: '*.tar'
            }
        }
        stage('Publish nightly builds to NAS1/Nexus') {
            when {
                anyOf {
                    branch '**/e2e_master'
                }
            }
            steps {
                cifsPublisher alwaysPublishFromMaster: false, continueOnError: false, failOnError: false,
                        paramPublish: null, masterNodeName: '',
                        publishers: [[configName: 'NAS1', transfers:
                                [
                                    [sourceFiles    : 'distro/tomcat8/target/api-mgmt-tomcat*.zip',
                                     removePrefix   : 'distro/tomcat8/target',
                                     remoteDirectory: "api-mgmt/nightlyBuilds/${PROJECT_VERSION}-${GIT_COMMIT_SHORT}"],
                                    [sourceFiles    : 'setups/target/api-mgmt*.zip',
                                     removePrefix   : 'setups/target',
                                     remoteDirectory: "api-mgmt/nightlyBuilds/${PROJECT_VERSION}-${GIT_COMMIT_SHORT}"],
                                    [sourceFiles    : '*.tar',
                                     removePrefix   : '',
                                     remoteDirectory: "api-mgmt/nightlyBuilds/${PROJECT_VERSION}-${GIT_COMMIT_SHORT}"]
                                ]
                         ]]

                withDockerRegistry([credentialsId: 'nexus', url: "https://gitlab.scheer-group.com:8080"]) {
                    sh './ci/publish-images.sh ${PROJECT_VERSION}'
                }
            }
        }

        stage('Deployment to Apitest') {
            when {
              branch '**/e2e_master'
            }
            steps {
                script {
                    sh 'ssh -i ~/.ssh/apiteste2ech apimgmt@apitest.e2e.ch "bash -s" < ./ci/deploy-api-mgmt.sh "/home/apimgmt/api-mgmt/single-host-setup/" "${PROJECT_VERSION}" "${GIT_COMMIT_SHORT}" "${BUILD_NUMBER}" "${BRANCH_NAME}"'
                }
            }
        }

        stage('Publish release builds to NAS1/Nexus') {
            when {
                anyOf {
                    branch '**/e2e_release'
                }
            }
            steps {
                cifsPublisher alwaysPublishFromMaster: false, continueOnError: false, failOnError: false,
                        paramPublish: null, masterNodeName: '',
                        publishers: [[configName: 'NAS1', transfers:
                                [
                                        [sourceFiles    : 'distro/tomcat8/target/api-mgmt-tomcat*.zip',
                                         removePrefix   : 'distro/tomcat8/target',
                                         remoteDirectory: "api-mgmt/${PROJECT_VERSION}"],
                                        [sourceFiles    : 'setups/target/api-mgmt*.zip',
                                         removePrefix   : 'setups/target',
                                         remoteDirectory: "api-mgmt/${PROJECT_VERSION}"],
                                        [sourceFiles    : '*.tar',
                                         removePrefix   : '',
                                         remoteDirectory: "api-mgmt/${PROJECT_VERSION}"]
                                ]
                         ]]

                withDockerRegistry([credentialsId: 'nexus', url: "https://gitlab.scheer-group.com:8080"]) {
                    sh './ci/publish-images.sh ${PROJECT_VERSION} release'
                }
            }
        }

        stage('Remove docker images') {
            steps {
                script {
                    sh """
                        docker image rm -f api-mgmt/ui:${PROJECT_VERSION}
                        docker image rm -f api-mgmt/keycloak:${PROJECT_VERSION}
                        docker image rm -f api-mgmt/gateway:${PROJECT_VERSION}
                    """
                }
            }
        }
    }

    post {
        always {
            script {
              jenkinsJiraIntegration(['JiraSiteName': 'Jira'])
            }
        }
        unstable {
            emailext to: 'florian.volk@scheer-group.com, benjamin.kihm@scheer-group.com',
                     subject: '${DEFAULT_SUBJECT}',
                     body: '${DEFAULT_CONTENT}'
        }
        failure {
            emailext to: 'florian.volk@scheer-group.com, benjamin.kihm@scheer-group.com',
                     subject: '${DEFAULT_SUBJECT}',
                     body: '${DEFAULT_CONTENT}'
        }
        fixed {
          emailext to: 'florian.volk@scheer-group.com, benjamin.kihm@scheer-group.com',
                   subject: '${DEFAULT_SUBJECT}',
                   body: '${DEFAULT_CONTENT}'
        }
    }
}
