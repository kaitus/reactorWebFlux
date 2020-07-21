pipeline {
    agent any
    stages {
        stage('Checkout') {
            steps { //Checking out the repo
                git 'https://github.com/kaitus/reactorWebFlux.git'
            }
        }

        stage ('Build') {
            steps {
                sh 'mvnw.cmd clean package' 
            }
        }
    }
}
