FROM tomcat:7.0.63

RUN apt-get update &&\
    apt-get install -y default-jdk &&\
    apt-get install -y maven && \
    useradd -m -d /home/tomcat -s /bin/bash tomcat
ADD . /usr/src/app
WORKDIR /usr/src/app
RUN chown -R tomcat.tomcat /usr/src/app && \
    mvn package && \
    rm -rf /usr/local/tomcat/webapps/ROOT

COPY ./target/weather-demo-0.0.1-SNAPSHOT /usr/local/tomcat/webapps/ROOT

#CMD /usr/local/tomcat/bin/startup.sh && tail -f /usr/local/tomcat/logs/catalina.out

#USER tomcat
EXPOSE 8080
CMD ["catalina.sh", "run"]
