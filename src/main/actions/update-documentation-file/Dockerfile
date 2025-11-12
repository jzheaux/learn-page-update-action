FROM gradle:8.5.0-jdk21-alpine as build
COPY src /app/src/
COPY config /app/config/
COPY build.gradle settings.gradle gradle.properties entrypoint.sh /app/
RUN cd /app && gradle -Dorg.gradle.welcome=never --no-daemon bootJar

FROM ghcr.io/bell-sw/liberica-openjdk-debian:21.0.2-14
COPY --from=build /app/build/libs/releases-api-sync-action.jar /opt/action/releases-api-sync.jar
COPY --from=build /app/entrypoint.sh /opt/action/entrypoint.sh
ENTRYPOINT ["/opt/action/entrypoint.sh"]
