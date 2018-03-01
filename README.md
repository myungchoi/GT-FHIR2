GT-FHIR2: Georgia Tech - OmopOnFHIR Implementation version 2
- Supports OMOP v5
- Supports STU3

How to install and run.

1. Install and configure OMOP V5 database with Concept table populated. 
2. Create f_person table and populate if person table is not empty. person<->f_person is one-to-one.
3. There are two projects. gt-fhir2-jpa and gt-fhir2-server. Go into these directories. And run "mvn clean install"
4. In gt-fhir2-server directory, run "mvn jetty:run"

To test:
UI - http://localhost:8080/gt-fhir/tester/
API -  	http://localhost:8080/gt-fhir/fhir

Status: work-in-progress

Patient: 
- Write a single patient
- Search without and with parameter: (by family name) adding more

Organization:
- Basic read.

Please contact myung.choi@gtri.gatech.edu if you want to participate in developing OmopOnFHIR for STU3.
