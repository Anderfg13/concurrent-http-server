FROM maven:3.9-amazoncorretto-21 AS build
WORKDIR /app
COPY pom.xml ./
COPY src ./src
RUN mvn -q -DskipTests clean package

FROM amazoncorretto:21
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

ENV PORT=8080
ENV APP_ENV=production

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
