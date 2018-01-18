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
                sh 'mvn package'
                sh 'mvn verify'

                archiveArtifacts artifacts: 'distro/tomcat8/target/apiman-distro-tomcat*.zip'
            }
        }
    }
}
