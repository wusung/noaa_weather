FROM java:openjdk-8-jdk

RUN apt-get update &&\
    apt-get install -y maven 
ADD . /app
WORKDIR /app
RUN mvn package
RUN bin/country-cli.sh
RUN bin/station-cli.sh
