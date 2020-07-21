pipeline {

    agent any

    stages {
        stage('Checkout') {
            steps { //Checking out the repo
                git 'https://github.com/kaitus/spring-boot-api-example.git'
            }
        }
	stage('compile') {
            steps { //Compile application
                bat 'mvn -Dmaven.test.failure.ignore=true install'
            }
        }
}
