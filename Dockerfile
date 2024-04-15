#FROM openjdk:8-jdk-alpine
FROM adoptopenjdk/openjdk8
VOLUME /tmp
ADD target/otceditor.jar otceditor.jar
EXPOSE 8081

#ARG OTCHOME
ENV OTC_HOME=/tmp
#RUN ls ${OTC_HOME}

ENTRYPOINT ["java", "-jar", "/otceditor.jar"]

# to build:
#    - docker build -t otceditor:latest -f Dockerfile .
# to run on windows:
#    - docker run -p 8081:8081 otceditor -v %OTC_HOME%:/tmp
# to run on Linux:
#    - docker run -p 8081:8081 otceditor -v ${OTC_HOME}:tmp
# to push for java 8 to docker hub
#    - docker push otceditor8:1.0
# to push for java 11 to docker hub
#    - docker push otceditor11:1.0
# to push for java 12 to docker hub
#    - docker push otceditor12:1.0
