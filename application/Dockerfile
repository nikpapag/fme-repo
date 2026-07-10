FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY target/*.jar /app/app.jar

ENV JAVA_OPTS=""
ENV SERVER_PORT=8080

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar --server.port=${SERVER_PORT}"]
