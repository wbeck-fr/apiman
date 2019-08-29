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
        lock('ApiMgmt-Build')
    }

    stages {
        stage('Clean') {
            steps {
                sh 'git clean -xdf'
            }
        }

        stage('Tests') {
            parallel {
                stage('Test local'){
                    steps {
                        sh 'mvn clean test'
                    }
                    post {
                        always {
                            junit '**/target/surefire-reports/*.xml'
                        }
                    }
                }

                stage('ES tests in docker'){
                    environment {
                        JAVA_HOME = '/usr/local/openjdk-8'
                    }
                    agent {
                        // https://github.com/carlossg/docker-maven#running-as-non-root
                        docker {
                            image 'maven:3-jdk-8'
                            args '-v $HOME/.m2:/var/maven/.m2 --tmpfs /.cache -e MAVEN_CONFIG=/var/maven/.m2 -e MAVEN_OPTS="-Duser.home=/var/maven"'
                            label 'docker'
                        }
                    }
                    steps {
                        retry(2) {
                            sh 'mvn clean test -pl !gateway/engine/redis -Dapiman-test.type=es -Dapiman.gateway-test.config=servlet-es'
                        }
                    }
                    post {
                        always {
                            junit '**/target/surefire-reports/*.xml'
                        }
                    }
                }
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean install -DskipTests'
            }
        }

         stage('Trigger Apiman-Plugins-Pipeline') {
             parallel {
                 stage('Release Build') {
                     when {
                         anyOf {
                             branch '**/e2e_release'
                         }
                     }
                     steps {
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
                         build job: '../Apiman-Plugins-Pipeline/e2e_master', wait: true
                     }
                 }
             }
        }

        stage('Build docker images') {
            steps {
                sh """
                    mvn clean package docker:build -P docker -pl !distro/wildfly10,!distro/wildfly11,!distro/eap7 -DskipTests
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
                    sh './publishImages.sh ${PROJECT_VERSION}'
                }
            }
        }

        stage('Deployment to Apitest') {
                when {
                  branch 'e2e_master'
                }
                steps {
                    script {
                        sh 'ssh -i ~/.ssh/apiteste2ech apimgmt@apitest.e2e.ch "bash -s" < ~/autoDeploy/deployApiMgmt.sh "/home/apimgmt/api-mgmt/single-host-setup/" "${PROJECT_VERSION}" "${GIT_COMMIT_SHORT}" "${BUILD_NUMBER}"'
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
                    sh './publishImages.sh ${PROJECT_VERSION} release'
                }
            }
        }
    }

    post {
        always {
            script {
                def fieldsResponse = jiraGetFields site: 'Jira'

                def jiraTicketKeys = jiraIssueSelector(issueSelector: [$class: 'DefaultIssueSelector'])
                jiraTicketKeys.each { issueTicketKey ->
                def customFieldBuildStatus = getFieldId(fieldsResponse.data, 'Build Status')
                def customFieldBuild = getFieldId(fieldsResponse.data, 'Build')

                def date = new Date().format("YYYY-MM-dd'T'HH:mm:ss.sZZZ")
                def badgeURL = '!' + env.JENKINS_URL + 'buildStatus/icon?job=' + URLEncoder.encode(env.JOB_NAME, "UTF-8") + '!'
                def badgeLink = '[' + badgeURL + '|' + env.BUILD_URL + ']'

                def updateJenkinsField = [fields: ["${customFieldBuildStatus}": "${badgeLink}", "${customFieldBuild}": "${date}"]]
                jiraEditIssue idOrKey: issueTicketKey, issue: updateJenkinsField, site: 'Jira'
                }
            }
        }
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
        fixed {
          emailext to: 'florian.volk@scheer-group.com, benjamin.kihm@scheer-group.com',
                   recipientProviders: [[$class: 'CulpritsRecipientProvider']],
                   subject: '${DEFAULT_SUBJECT}',
                   body: '${DEFAULT_CONTENT}'
        }
    }
}

// Get a Custom field id from fields based on the field name.
def getFieldId(fields, fieldName) {
  for (i = 0; i <fields.size(); i++) {
    if(fields[i].custom && fields[i].name == fieldName) {
      return fields[i].id
    }
  }
}
