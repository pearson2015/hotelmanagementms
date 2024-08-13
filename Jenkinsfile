pipeline {
    agent any
    environment {
        registry = "mydarkworld/hotelmanagementms"
        registryCredential = "mydockerhub"
        dockerImage = ''
    }
    stages {
        stage('Checkout') {
            steps {
                checkout scmGit(branches: [[name: '*/main']], extensions: [], userRemoteConfigs: [[credentialsId: 'mygithubkey', url: 'git@github.com:pearson2015/hotelmanagementms.git']])
            }
        }
        
        stage('Build Jar') {
            steps {
                sh "mvn clean install"
            }
        }
        
        stage('Build Image') {
            steps {
                script {
                    dockerImage = docker.build registry
                }
            }
        }
        
        stage('Upload Image') {
            steps {
                script {
                    docker.withRegistry( '', registryCredential ) { 
                        dockerImage.push()
                    }
                }
            }
        }
        
        
        stage('Deploy App on k8s') {
            steps {
                withCredentials([string(credentialsId: 'my_kubernetes', variable: 'api_token')]) {
                    sh 'helm --kube-token $api_token --kube-apiserver https://192.168.76.2:8443 --kube-insecure-skip-tls-verify=true upgrade hotelmanagementms-release --install k8chart --set image.tag=latest'
                }
            }
        }
    }
    post { 
        cleanup {
            /* clean up our workspace */
            deleteDir()
            /* clean up tmp directory */
            dir("${workspace}@tmp") {
                deleteDir()
            }
            /* clean up script directory */
            dir("${workspace}@script") {
                deleteDir()
            }
        }
    }
}
