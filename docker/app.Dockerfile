# syntax=docker/dockerfile:1

# Build one selected Maven module from the repository root. The same image
# recipe is reused by the module compose overlay through the MODULE argument.
FROM maven:3.9-eclipse-temurin-21 AS build

ARG MODULE
WORKDIR /workspace
COPY . .

RUN mvn -q -pl "${MODULE}" -am package -DskipTests \
    && find "/workspace/${MODULE}/target" -maxdepth 1 -type f -name '*.jar' ! -name '*original*' -exec cp {} /tmp/app.jar \;

FROM eclipse-temurin:21-jre

WORKDIR /app
RUN mkdir -p /config-repo
COPY --from=build /tmp/app.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
