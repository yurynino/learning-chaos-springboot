FROM openjdk:8-jdk-alpine
COPY target/gateway-service-1.0-SNAPSHOT.jar gateway-service.jar
ENTRYPOINT ["java","-jar","/gateway-service.jar"]