FROM openjdk:8-jdk-alpine

ADD target/*.jar app.jar

ENTRYPOINT ["java","-Xmx512m","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
