FROM openjdk:11-jdk-slim
VOLUME /tmp


EXPOSE 8000
ADD target/authServer-0.0.1-SNAPSHOT.jar authServer-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/authServer-0.0.1-SNAPSHOT.jar"]
