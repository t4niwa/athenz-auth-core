FROM docker.io/library/maven:3-eclipse-temurin-11-focal as builder

ARG VERSION=
ARG ATHENZ_VERSION=
ARG JAVA_VERSION=11
# date -u +'%Y-%m-%dT%H:%M:%SZ'
ARG BUILD_DATE
# git rev-parse --short HEAD
ARG VCS_REF

LABEL org.opencontainers.image.version=$VERSION
LABEL org.opencontainers.image.revision=$VCS_REF
LABEL org.opencontainers.image.created=$BUILD_DATE
LABEL org.opencontainers.image.title="Athenz Auth Core"
LABEL org.opencontainers.image.authors="t4niwa <t4niwa@duck.com>"
LABEL org.opencontainers.image.vendor="t4niwa <t4niwa@duck.com>"
LABEL org.opencontainers.image.licenses="Private"
LABEL org.opencontainers.image.url="ghcr.io/t4niwa/athenz-auth-core"
LABEL org.opencontainers.image.documentation="https://www.athenz.io/"
LABEL org.opencontainers.image.source="https://github.com/AthenZ/athenz"

COPY . .

RUN curl -s https://webi.sh/yq | sh && $HOME/.local/bin/yq -v

RUN cat pom.xml.template \
      | $HOME/.local/bin/yq -p xml -o xml ".project.version=strenv(VERSION)" \
      | $HOME/.local/bin/yq -p xml -o xml ".project.properties.\"athenz.version\"=strenv(ATHENZ_VERSION)" \
      | $HOME/.local/bin/yq -p xml -o xml ".project.properties.\"java.version\"=strenv(JAVA_VERSION)" \
      | tee pom.xml

ENV MAVEN_CONFIG "$HOME/.m2"

RUN mvn -B package --file pom.xml

FROM docker.io/library/openjdk:22-slim-bullseye

ARG VERSION

COPY --from=builder /target/athenz-auth-core-$VERSION.jar /target/athenz-auth-core-$VERSION.jar

ENV JAR_DESTINATION "/"

ENTRYPOINT ["/bin/sh", "-c", "cp -p /target/athenz-auth-core-*.jar ${JAR_DESTINATION}/athenz-auth-core.jar"]
