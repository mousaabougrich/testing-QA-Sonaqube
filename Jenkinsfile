pipeline {
    agent any

    triggers {
        pollSCM('H/5 * * * *')
    }

    environment {
        SONAR_HOST = 'sonarqube'  // Using Docker service name instead of IP
        SONAR_TOKEN = credentials('retail-token')  // Jenkins credential ID
        MAVEN_OPTS = '-Xmx512m -XX:MaxMetaspaceSize=256m'
    }

    stages {
        stage('Checkout') {
            steps {
                echo '📥 Cloning repository...'
                checkout scm
            }
        }

        stage('Build & Test & Analyze') {
            steps {
                echo '🚀 Running Maven build with tests and SonarQube...'
                script {
                    try {
                        sh '''
                            mvn clean verify sonar:sonar \
                            -Dspring.profiles.active=test \
                            -Dsonar.projectKey=biochain \
                            -Dsonar.host.url=http://${SONAR_HOST}:9000 \
                            -Dsonar.token=${SONAR_TOKEN}
                        '''
                    } catch (Exception e) {
                        echo "Build failed: ${e.getMessage()}"
                        currentBuild.result = 'FAILURE'
                    }
                }
            }
        }
    }

    post {
        success {
            echo '✅ Build completed successfully!'
            echo "📊 View SonarQube: http://${SONAR_HOST}:9000/dashboard?id=biochain"
        }
        failure {
            echo '❌ Build failed - Check console output'
        }
    }
}
