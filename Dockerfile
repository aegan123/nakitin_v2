FROM eclipse-temurin:21 AS build
WORKDIR /work

COPY gradle gradle
COPY gradlew .
COPY build.gradle . 
COPY settings.gradle .
COPY src src

RUN ./gradlew bootJar

FROM eclipse-temurin:21
WORKDIR /app
COPY --from=build /work/build/libs/nakitin.jar .

ENTRYPOINT ["java","-jar","nakitin.jar"]

# add image to repository
LABEL org.opencontainers.image.source=https://github.com/asteriskiry/nakitin_V2
