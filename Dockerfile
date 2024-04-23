# Build stage
FROM maven:3-amazoncorretto-21 AS build
COPY src /app/src
COPY pom.xml /app
RUN mvn -f /app/pom.xml clean package

# Package stage
FROM amazoncorretto:21
COPY --from=build /app/target/*jar-with-dependencies.jar app.jar
EXPOSE 7071
ENTRYPOINT ["java", "-jar", "app.jar", "-XX:ActiveProcessorCount=2", "-Xss512k", "-XX:+UseZGC", "-XX:+ZGenerational", "-XX:+TieredCompilation", " -XX:+UseCGroupMemoryLimitForHeap", "-XX:TieredStopAtLevel=1"]