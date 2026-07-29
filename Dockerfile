FROM eclipse-temurin:21-jdk AS runtime
WORKDIR /app
COPY build/libs/Discord-Manager-BE-1.0.0.jar /app/Discord-Manager-BE.jar
RUN pwd
ENTRYPOINT ["java", "-jar", "/app/Discord-Manager-BE.jar"]