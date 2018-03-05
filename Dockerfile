#Build the Maven project
FROM maven:3.5.2-alpine as builder
COPY . /usr/src/app
WORKDIR /usr/src/app
RUN mvn install

#Build the Tomcat container
FROM tomcat:alpine
#set environment variables
#      JDBC_URL=jdbc:postgresql://data.hdap.gatech.edu:5435/omop_v5
ENV JDBC_URL=jdbc:postgresql://data.hdap.gatech.edu:5438/postgres?currentSchema=omop_v5 JDBC_USERNAME=omop_v5 JDBC_PASSWORD=i3lworks
RUN apk update
RUN apk add zip postgresql-client

# Copy GT-FHIR war file to webapps.
COPY --from=builder /usr/src/app/gt-fhir2-server/target/gt-fhir2-server.war $CATALINA_HOME/webapps/gt-fhir.war

EXPOSE 8080