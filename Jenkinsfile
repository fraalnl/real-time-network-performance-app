pipeline {
    agent any

    tools {
        maven "Maven-3.9.5"
    }

    environment {  // available through env
        SONAR_HOST_URL = 'http://localhost:9000'
        SONARQUBE_TOKEN = credentials('jenkins-network-performance') //configured in Jenkins
        SONAR_PROJECT_KEY = 'jenkins-network-performance' //configured in sonarqube
        DOCKER_IMAGE = 'network-performance-dashboard'
        DOCKER_TAG = 'latest'
//        DOCKERHUB_CREDENTIALS = 'dockerhub-credentials' // Jenkins credentials for Docker Hub login, configured
//        DOCKERHUB_USERNAME = 'fraalnl'
    }

    stages {
        stage('Build') {
            steps {
                checkout([$class: 'GitSCM',
                          userRemoteConfigs: [[
                                                      url: 'https://github.com/fraalnl/real-time-network-performance-app',
                                              ]],
                          branches: [[name: 'development']]])
                bat "mvn clean package -DskipTests"
            }
        }

        stage('Unit Tests') {
            steps {
                bat 'mvn test'
            }
            post {
                always {
                    echo 'Unit tests completed'
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Integration Tests & Publish Coverage') {
            steps {
                bat 'mvn verify -Dsurefire.useFile=false -Dfailsafe.useFile=false'
            }
            post {
                always {
                    echo 'Integration tests completed'
                    junit '**/target/failsafe-reports/*.xml'
                    recordCoverage(
                            tools: [[parser: 'JACOCO']],
                            id: 'jacoco',
                            name: 'JaCoCo Coverage',
                            sourceCodeRetention: 'EVERY_BUILD'
                    ) // Publishes JaCoCo merged report using Coverage Plugin, replacing deprecated Jacoco plugin
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                bat """
                mvn sonar:sonar ^
                  -Dsonar.token=%SONARQUBE_TOKEN% ^
                  -Dsonar.host.url=%SONAR_HOST_URL% ^
                  -Dsonar.projectKey=%SONAR_PROJECT_KEY% ^
                  -Dsonar.coverage.jacoco.xmlReportPaths=target/jacoco-report-merged/jacoco.xml ^
                  -Dsonar.java.binaries=target/classes
                """
            }
            post {
                always {
                    echo 'SonarQube analysis completed'
                }
            }
        }

        stage('Docker Build') {
            steps {
                script {
                    String imageName = "${env.DOCKER_IMAGE}:${env.DOCKER_TAG}"
                    echo "Building Docker image: ${imageName}"
                    bat "docker build -t ${imageName} ."
                    echo "Docker build complete"
                }
            }
        }

        stage('Docker Compose Up') {
            steps {
                echo 'Starting Docker containers with docker-compose...'
                bat 'docker-compose up -d'
            }
        }

//        stage('Docker Hub Login & Push') {
//            steps {
//                script {
//                    withCredentials([usernamePassword(credentialsId: env.DOCKERHUB_CREDENTIALS, usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
//                        bat "docker login -u %DOCKER_USER% -p %DOCKER_PASS%"
//                        String imageName = "${env.DOCKER_IMAGE}:${env.DOCKER_TAG}"
//                        bat "docker push ${imageName}"
//                    }
//                }
//            }
//        }

        stage('Archive Artifacts') {
            steps {
                archiveArtifacts artifacts: '**/target/surefire-reports/**, **/target/failsafe-reports/**, **/target/site/jacoco/**, **/target/*.jar', fingerprint: true
                echo 'Artifacts archived successfully'
            }
        }
    }

    post {
        always {
            echo "Build completed with status: ${currentBuild.result}"
        }
    }
}