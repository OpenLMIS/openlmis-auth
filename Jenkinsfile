#!/usr/bin/env groovy

properties([
  [
    $class: 'ThrottleJobProperty',
    categories: ['pipeline'],
    throttleEnabled: true,
    throttleOption: 'category'
  ]
])
pipeline {
    agent none
    options {
        buildDiscarder(logRotator(numToKeepStr: '15'))
        disableConcurrentBuilds()
    }
    environment {
        COMPOSE_PROJECT_NAME = "auth${BRANCH_NAME}"
    }
    parameters {
        string(name: 'contractTestsBranch', defaultValue: 'master', description: 'The branch of contract tests to checkout')
    }
    stages {
        stage('Preparation') {
            agent any
            steps {
                withCredentials([usernamePassword(
                  credentialsId: "cad2f741-7b1e-4ddd-b5ca-2959d40f62c2",
                  usernameVariable: "USER",
                  passwordVariable: "PASS"
                )]) {
                    sh 'set +x'
                    sh 'docker login -u $USER -p $PASS'
                }
                script {
                    CURRENT_BRANCH = env.GIT_BRANCH // needed for agent-less stages
                    def properties = readProperties file: 'gradle.properties'
                    if (!properties.serviceVersion) {
                        error("serviceVersion property not found")
                    }
                    VERSION = properties.serviceVersion
                    STAGING_VERSION = properties.serviceVersion
                    if (CURRENT_BRANCH != 'master' || (CURRENT_BRANCH == 'master' && !VERSION.endsWith("SNAPSHOT"))) {
                        STAGING_VERSION += "-STAGING"
                    }
                    currentBuild.displayName += " - " + VERSION
                }
            }
            post {
                failure {
                    script {
                        notifyAfterFailure()
                    }
                }
            }
        }
        stage('Build') {
            agent any
            environment {
                PATH = "/usr/local/bin/:$PATH"
                STAGING_VERSION = "${STAGING_VERSION}"
            }
            steps {
                withCredentials([file(credentialsId: '8da5ba56-8ebb-4a6a-bdb5-43c9d0efb120', variable: 'ENV_FILE')]) {
                     sh( script: "./ci-buildImage.sh" )
                }
            }
            post {
                success {
                    archive 'build/libs/*.jar,build/resources/main/api-definition.html, build/resources/main/  version.properties'
                }
                failure {
                    script {
                        notifyAfterFailure()
                    }
                }
                always {
                    checkstyle pattern: '**/build/reports/checkstyle/*.xml'
                    pmd pattern: '**/build/reports/pmd/*.xml'
                    junit '**/build/test-results/*/*.xml'
                }
            }
        }
        stage('Build demo-data') {
            when {
                expression {
                    return CURRENT_BRANCH == 'master'
                }
            }
            steps {
                build job: "OpenLMIS-3.x-build-demo-data-pipeline"
            }
            post {
                failure {
                    script {
                        notifyAfterFailure()
                    }
                }
            }
        }
        stage('Deploy to test') {
            when {
                expression {
                    return CURRENT_BRANCH == 'master' && VERSION.endsWith("SNAPSHOT")
                }
            }
            steps {
                build job: 'OpenLMIS-auth-deploy-to-test', wait: false
            }
            post {
                failure {
                    script {
                        notifyAfterFailure()
                    }
                }
            }
        }
        stage('Parallel: Sonar analysis and contract tests') {
            parallel {
                stage('Sonar analysis') {
                    agent any
                    environment {
                        PATH = "/usr/local/bin/:$PATH"
                    }
                    steps {
                        withSonarQubeEnv('Sonar OpenLMIS') {
                            withCredentials([string(credentialsId: 'SONAR_LOGIN', variable: 'SONAR_LOGIN'), string(credentialsId: 'SONAR_PASSWORD', variable: 'SONAR_PASSWORD')]) {
                                sh(script: "./ci-sonarAnalysis.sh")

                                // workaround: Sonar plugin retrieves the path directly from the output
                                sh 'echo "Working dir: ${WORKSPACE}/build/sonar"'
                            }
                        }
                        timeout(time: 1, unit: 'HOURS') {
                            script {
                                def gate = waitForQualityGate()
                                if (gate.status != 'OK') {
                                    error 'Quality Gate FAILED'
                                }
                            }
                        }
                    }
                    post {
                        failure {
                            script {
                                notifyAfterFailure()
                            }
                        }
                    }
                }
                stage('Contract tests') {
                    steps {
                        build job: "OpenLMIS-contract-tests-pipeline/${params.contractTestsBranch}", propagate: true, wait: true,
                        parameters: [
                            string(name: 'serviceName', value: 'auth'),
                            text(name: 'customEnv', value: "OL_AUTH_VERSION=${STAGING_VERSION}")
                        ]
                        build job: "OpenLMIS-contract-tests-pipeline/${params.contractTestsBranch}", propagate: true, wait: true,
                        parameters: [
                            string(name: 'serviceName', value: 'referencedata'),
                            text(name: 'customEnv', value: "OL_AUTH_VERSION=${STAGING_VERSION}")
                        ]
                    }
                    post {
                        failure {
                            script {
                                notifyAfterFailure()
                            }
                        }
                    }
                }
            }
        }
        stage('ERD generation') {
            agent {
                node {
                    label 'master'
                }
            }
            environment {
                PATH = "/usr/local/bin/:$PATH"
            }
            steps {
                dir('erd') {
                    sh(script: "../ci-erdGeneration.sh")
                    archiveArtifacts artifacts: 'erd-auth.zip'
                }
            }
            post {
                failure {
                    script {
                        notifyAfterFailure()
                    }
                }
            }
        }
        stage('Push image') {
            agent any
            when {
                expression {
                    env.GIT_BRANCH =~ /rel-.+/ || (env.GIT_BRANCH == 'master' && !VERSION.endsWith("SNAPSHOT"))
                }
            }
            steps {
                sh "docker pull openlmis/auth:${STAGING_VERSION}"
                sh "docker tag openlmis/auth:${STAGING_VERSION} openlmis/auth:${VERSION}"
                sh "docker push openlmis/auth:${VERSION}"
            }
            post {
                success {
                    script {
                        if (!VERSION.endsWith("SNAPSHOT")) {
                            currentBuild.setKeepLog(true)
                        }
                    }
                }
                failure {
                    script {
                        notifyAfterFailure()
                    }
                }
            }
        }
    }
    post {
        fixed {
            script {
                BRANCH = "${BRANCH_NAME}"
                if (BRANCH.equals("master") || BRANCH.startsWith("rel-")) {
                    slackSend color: 'good', message: "${env.JOB_NAME} - #${env.BUILD_NUMBER} Back to normal"
                }
            }
        }
    }
}

def notifyAfterFailure() {
    BRANCH = "${BRANCH_NAME}"
    if (BRANCH.equals("master") || BRANCH.startsWith("rel-")) {
        slackSend color: 'danger', message: "${env.JOB_NAME} - #${env.BUILD_NUMBER} ${env.STAGE_NAME} FAILED (<${env.BUILD_URL}|Open>)"
    }
    emailext subject: "${env.JOB_NAME} - #${env.BUILD_NUMBER} ${env.STAGE_NAME} FAILED",
        body: """<p>${env.JOB_NAME} - #${env.BUILD_NUMBER} ${env.STAGE_NAME} FAILED</p><p>Check console <a href="${env.BUILD_URL}">output</a> to view the results.</p>""",
        recipientProviders: [[$class: 'CulpritsRecipientProvider'], [$class: 'DevelopersRecipientProvider']]
}