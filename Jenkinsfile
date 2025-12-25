pipeline {
    agent any

    triggers {
        pollSCM('H/5 * * * *')
    }

    environment {
        SONAR_HOST = 'sonarqube'
        SONAR_TOKEN = credentials('retail-token')
        MAVEN_OPTS = '-Xmx1024m -XX:MaxMetaspaceSize=512m'
    }

    stages {
        stage('Checkout') {
            steps {
                echo '📥 Cloning repository from GitHub...'
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                echo '🔨 Building and running tests with coverage...'
                script {
                    try {
                        // ✅ Exécute les tests + génère le rapport JaCoCo
                        sh 'mvn clean test -Dspring.profiles.active=test'
                    } catch (Exception e) {
                        echo "⚠️ Some tests failed but continuing: ${e.getMessage()}"
                        currentBuild.result = 'UNSTABLE'
                    }
                }
            }
        }

        stage('Package') {
            steps {
                echo '📦 Packaging application...'
                // Utilise les classes déjà compilées, skip les tests (déjà faits)
                sh 'mvn package -DskipTests'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                echo '🔍 Running SonarQube analysis with coverage...'
                sh '''
                    mvn sonar:sonar \
                    -Dsonar.projectKey=biochain \
                    -Dsonar.host.url=http://${SONAR_HOST}:9000 \
                    -Dsonar.token=${SONAR_TOKEN}
                '''
            }
        }
    }

    post {
        success {
            echo '✅ Build completed successfully!'
            echo '📊 View SonarQube: http://localhost:9000/dashboard?id=biochain'
        }
        unstable {
            echo '⚠️ Build unstable - Some tests failed but analysis completed'
            echo '📊 View SonarQube: http://localhost:9000/dashboard?id=biochain'
        }
        failure {
            echo '❌ Build failed - Check console output'
        }
    }
}
