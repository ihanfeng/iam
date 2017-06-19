pipeline {

  agent { label 'maven' }

  options {
    timeout(time: 1, unit: 'HOURS')
    buildDiscarder(logRotator(numToKeepStr: '5')) 
  }

  stages {
    stage('checkout') {
      steps {
        git(url: 'https://github.com/indigo-iam/iam.git', branch: env.BRANCH_NAME)
        stash name: 'code', useDefaultExcludes: false
      }
    }

    stage('build') {
      steps {
        unstash 'code'
        sh 'mvn -B clean compile'
      }
    }

    stage('test') {
      steps {
        unstash 'code'
        sh 'mvn -B clean test'
      }

      post {
        always {
          junit '**/target/surefire-reports/TEST-*.xml'
        }
      }
    }


    stage('package') {
      steps {
        unstash 'code'
        sh 'mvn -B -DskipTests=true clean package'
        archive 'iam-login-service/target/iam-login-service.war'
        archive 'iam-login-service/target/classes/iam.version.properties'
        archive 'iam-test-client/target/iam-test-client.jar'
        stash includes: 'iam-login-service/target/iam-login-service.war,iam-login-service/target/classes/iam.version.properties,iam-test-client/target/iam-test-client.jar', name: 'iam-artifacts'
      }
    }

    stage('docker-images') {
      agent { label 'docker' }
      steps {
        unstash 'code'
        unstash 'iam-artifacts'
        sh '''
        sed -i -e 's#iam\\.version#IAM_VERSION#' iam-login-service/target/classes/iam.version.properties
        source iam-login-service/target/classes/iam.version.properties
        export IAM_LOGIN_SERVICE_VERSION="v${IAM_VERSION}"

        /bin/bash iam-login-service/docker/build-prod-image.sh
        /bin/bash iam-login-service/docker/push-prod-image.sh
        /bin/bash iam-test-client/docker/build-prod-image.sh
        /bin/bash iam-test-client/docker/push-prod-image.sh
        '''
      }
    }
  }

  post {

    success {
      slackSend channel: "#iam", color: 'good', message: "${env.JOB_NAME} - #${env.BUILD_NUMBER} Success (<${env.BUILD_URL}|Open>)" 
    }

    unstable {
      slackSend channel: "#iam", color: 'danger', message: "${env.JOB_NAME} - #${env.BUILD_NUMBER} Unstable (<${env.BUILD_URL}|Open>)" 
    }

    failure {
      slackSend channel: "#iam", color: 'danger', message: "${env.JOB_NAME} - #${env.BUILD_NUMBER} Failure (<${env.BUILD_URL}|Open>)" 
    }
  }
}
