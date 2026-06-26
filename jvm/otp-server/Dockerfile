FROM amazoncorretto:23-alpine

WORKDIR /app

COPY modules/host/build/libs/host.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]