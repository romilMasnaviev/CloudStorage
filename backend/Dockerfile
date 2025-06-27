FROM amazoncorretto:21-alpine
WORKDIR /backend
COPY build/libs/backend-0.0.1-SNAPSHOT.jar /backend/backend.jar
ENTRYPOINT ["java","-jar","backend.jar"]