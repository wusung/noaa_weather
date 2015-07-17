FROM jdk:latest

RUN apt-get update &&\
    apt-get install -y default-jdk &&\
    apt-get install -y maven 
ADD . /app
WORKDIR /app
RUN mvn package
RUN bin/country-cli.sh
RUN bin/station-cli.sh
