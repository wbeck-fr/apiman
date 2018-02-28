#!groovy

pipeline {
    agent {
        node {
            label 'cscabbia'
            customWorkspace "workspace/Apiman-Pipeline"
        }
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
                sh 'mvn clean install -U'
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

                }
                archiveArtifacts artifacts: 'distro/tomcat8/target/E2EBridgeGateway*.zip'
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
                archiveArtifacts artifacts: 'distro/tomcat8/target/E2EBridgeGateway*.zip'
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
                                        [sourceFiles    : 'distro/tomcat8/target/E2EBridgeGateway*.zip',
                                         removePrefix   : 'distro/tomcat8/target',
                                         remoteDirectory: 'gateway']
                                ]
                             ]]
            }
        }
    }
}