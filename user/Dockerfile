FROM amazoncorretto:21-alpine
WORKDIR /user
COPY build/libs/user-0.0.1-SNAPSHOT.jar /user/user.jar
ENTRYPOINT ["java","-jar","user.jar"]