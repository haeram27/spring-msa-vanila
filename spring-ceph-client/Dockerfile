FROM eclipse-temurin:25-jre

ARG JAR_FILE=build/libs/*.jar

WORKDIR /app
COPY ${JAR_FILE} /app/app.jar

EXPOSE 18080

ENV JAVA_OPTS=""
ENV SPRING_ARGS=""

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app/app.jar ${SPRING_ARGS}"]
