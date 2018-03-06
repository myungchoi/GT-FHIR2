#!/usr/bin/env groovy
pipeline{
    agent any

    //Define stages for the build process
    stages{
        //Define the test stage
        stage('Test'){
            //Define the docker image to use for the test stage
            agent{
                docker{
                    image 'maven:3.5.2-alpine'
                }
            }
            //Write the scripts to run in the node Docker container to test the application.
            //Since this is a groovy file we use the '''string''' syntax to define multi-line formatting.
            //Groovy will use the string EXACTLY as written in between the ''' characters. In this instance each
            //line between the ''' characters will be treated as separate lines of a shell script.
            steps{
                sh '''mvn test'''
            }
        }

        //Define the deploy stage
        stage('Deploy'){
            steps{
                //The Jenkins Declarative Pipeline does not provide functionality to deploy to a private
                //Docker registry. In order to deploy to the HDAP Docker registry we must write a custom Groovy
                //script using the Jenkins Scripting Pipeline. This is done by placing Groovy code with in a "script"
                //element. The script below registers the HDAP Docker registry with the Docker instance used by
                //the Jenkins Pipeline, builds a Docker image of the project, and pushes it to the registry.
                script{
                    docker.withRegistry('https://apps2.hdap.gatech.edu'){
                        //Build and push the database image
                        def databaseImage = docker.build("gtfhir2:1.0", "-f Dockerfile .")
                        databaseImage.push('latest')
                    }
                }
            }
        }

        //Define stage to notify rancher
        stage('Notify'){
            steps{
                script{
                    rancher confirm: true, credentialId: 'rancher-server', endpoint: 'https://apps3.hdap.gatech.edu/v2-beta', environmentId: '1a16', environments: '', image: 'apps2.hdap.gatech.edu/gtfhir2:latest', ports: '', service: 'GT-FHIR-2/gtfhir2', timeout: 50
                }
            }
        }
    }
}