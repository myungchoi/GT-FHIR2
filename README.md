GT-FHIR2: Georgia Tech - OmopOnFHIR Implementation version 2
=
- Supports OMOP v5
- Supports STU3

Database Dependencies
-
This application requires an OMOP V5 database to work. You can use the database here: https://github.com/gt-health/smart-platform-docker/tree/hdap-devops/smart-postgresql
1. First clone the `hdap-devops` branch of the `smart-platform-docker project`
2. Navigate to the `smart-postgres` directory of the project.
   ```
     cd smart-postgres
   ```
3. Look at the docker-compose.yml file and adjust configurations as needed. By default the database is configured to run on port 5438. Use docker-compose to start the container.
   ```
   sudo docker-compose up --build -d
   ```
4. The OMOP V5 database is not running in a container.

How to install and run.
-
Docker Compose is used to create a container to run the GT-FHIR2 application. Before running the application
update the values of the JDBC_URL, JDBC_USERNAME, and JDBC_PASSWORD environment variables in the Dockerfile
They must contain the data necessary for your application to connect to an OMOP V5 database.
After updating the ENV variables in the Dockerfile start the application
```
sudo docker-compose up --build -d
```

Application URLs
-
UI - http://<my_host>:8080/gt-fhir/tester/
API -  	http://<my_host>:8080/gt-fhir/fhir

Additional Information
-
Status: work-in-progress

Patient: 
- Write a single patient
- Search without and with parameter: (by family name) adding more

Organization:
- Basic read.

Contributors:
- Lead: Myung Choi
- DevOp/Deployment: Eric Soto
- Docker/VM Management: Michael Riley
 
Please use Issues to report any problems. If you are willing to contribute, please fork this repository and do the pull requests.
