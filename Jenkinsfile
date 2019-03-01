#!/usr/bin/env groovy

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
    }

    stages {
        stage('Clean') {
            steps {
                sh 'git clean -xdf'
            }
        }
        stage('Test') {
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }
        stage('Build') {
            steps {
                sh 'mvn clean install'
            }
        }
        stage('Build docker images') {
            steps {
                sh """
                    mvn package docker:build -P docker -pl !distro/wildfly10,!distro/wildfly11,!distro/eap7
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
        stage('Publish nightly builds to NAS1') {
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
            }
        }
        stage('Publish release builds to NAS1') {
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
            }
        }
    }

    post {
        unstable {
            emailext to: 'florian.volk@scheer-group.com, benjamin.kihm@scheer-group.com',
                     recipientProviders: [[$class: 'CulpritsRecipientProvider']],
                     subject: '${DEFAULT_SUBJECT}',
                     body: '${DEFAULT_CONTENT}'
        }
        failure {
            emailext to: 'florian.volk@scheer-group.com, benjamin.kihm@scheer-group.com',
                     recipientProviders: [[$class: 'CulpritsRecipientProvider']],
                     subject: '${DEFAULT_SUBJECT}',
                     body: '${DEFAULT_CONTENT}'
        }
    }
}
