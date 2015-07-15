FROM java:openjdk-7u79-jdk

RUN apt-get update &&\
    apt-get install -y default-jdk &&\
    apt-get install -y maven 
ADD . /usr/src/app
WORKDIR /usr/src/app
RUN mvn package
RUN bin/country-cli.sh
RUN bin/station-cli.sh
