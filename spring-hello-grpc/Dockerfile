ARG DOCKER_REGISTRY=private.maven.com/docker-virtual-remotes
FROM ${DOCKER_REGISTRY}/eclipse-temurin:25-jdk-alpine
VOLUME /tmp
WORKDIR /app
COPY server/build/libs/*.jar app.jar
EXPOSE 8080 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
