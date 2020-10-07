#FROM openjdk:11-jdk-slim as build
#WORKDIR /workspace/app

#COPY mvnw .
#COPY .mvn .mvn
#COPY pom.xml .
#COPY src src

#RUN ./mvnw install -DskipTests
#RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)


FROM openjdk:11-jdk-slim
VOLUME /tmp
#ARG DEPENDENCY=/workspace/app/target/dependency
#COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
#COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
#COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app
#ENTRYPOINT ["java", "-cp", "app:app/lib/*", "com.idco.mesghal.Application"]

EXPOSE 8080
ADD target/mesghal-0.0.1-SNAPSHOT.jar mesghal-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/mesghal-0.0.1-SNAPSHOT.jar"]