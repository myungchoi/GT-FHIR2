GT-FHIR2: Georgia Tech - OmopOnFHIR Implementation version 2
- Supports OMOP v5
- Supports STU3

How to install and run.

1. Install and configure OMOP V5 database with Concept table populated. 
2. Create f_person table and populate if person table is not empty. person<->f_person is one-to-one.
3. There are two projects. gt-fhir2-jpa and gt-fhir2-server. Go into these directories. And run "mvn clean install"
4. In gt-fhir2-server directory, run "mvn jetty:run"


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
