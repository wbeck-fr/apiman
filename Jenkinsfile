#!groovy

pipeline {
    agent {
        node {
            label 'cscabbia'
            customWorkspace "workspace/Apiman-Pipeline"
        }
    }

    environment {
        JAVA_HOME = '/usr/lib/jvm/jdk1.8.0_191'
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
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
                sh 'mvn package docker:build -P docker -pl !distro/wildfly10,!distro/wildfly11,!distro/eap7'
                script {
                   MAVEN_VERSION = readMavenPom().getVersion()
                }
                sh "docker image save api-mgmt/ui:${MAVEN_VERSION} -o api-mgmt-ui-${MAVEN_VERSION}-overlay.tar"
                sh "docker image save api-mgmt/gateway:${MAVEN_VERSION} -o api-mgmt-gateway-${MAVEN_VERSION}-overlay.tar"
            }
        }
        stage('Archive') {
            when {
                not {
                    branch 'e2e_release'
                }
            }
            steps {
                script {
                    def commitID = "${env.GIT_COMMIT}"
                    def commitIDShort= commitID.substring(0,7)

                    dir('distro/tomcat8/target/') {
                        sh "rename.ul overlay ${commitIDShort} *.zip"
                    }
                    sh "rename.ul overlay ${commitIDShort} *.tar"
                }
                archiveArtifacts artifacts: 'distro/tomcat8/target/Scheer-PAS-API-Management*.zip'
                archiveArtifacts artifacts: '*.tar'
            }
        }
        stage('Archive release') {
            when {
                branch 'e2e_release'
            }
            steps {
                dir('distro/tomcat8/target/') {
                    sh 'rename.ul -- "-overlay" "" *.zip'
                }
                sh 'rename.ul -- "-overlay" "" *.tar'
                archiveArtifacts artifacts: 'distro/tomcat8/target/Scheer-PAS-API-Management*.zip'
                archiveArtifacts artifacts: '*.tar'
            }
        }
        stage('Publish NAS1') {
            when {
                anyOf {
                    branch '**/e2e_master'
                    branch 'e2e_release'
                }
            }
            steps {
                cifsPublisher alwaysPublishFromMaster: false, continueOnError: false, failOnError: false,
                        paramPublish: null, masterNodeName: '',
                        publishers: [[configName: 'NAS1', transfers:
                                [
                                    [sourceFiles    : 'distro/tomcat8/target/Scheer-PAS-API-Management*.zip',
                                     removePrefix   : 'distro/tomcat8/target',
                                     remoteDirectory: 'gateway'],
                                    [sourceFiles    : 'api-mgmt-ui*.tar',
                                     removePrefix   : '',
                                     remoteDirectory: 'gateway/docker'],
                                    [sourceFiles    : 'api-mgmt-gateway*.tar',
                                     removePrefix   : '',
                                     remoteDirectory: 'gateway/docker']
                                ]
                           ]]
            }
        }
    }
}