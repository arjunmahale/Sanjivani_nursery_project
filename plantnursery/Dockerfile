# Use a multi-stage build to produce a small runtime image
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /workspace
COPY pom.xml mvnw ./
COPY .mvn .mvn
# copy only pom first to cache dependencies
RUN mkdir -p /root/.m2
RUN mvn -v || true
COPY src src
RUN mvn -B -DskipTests clean package

# Runtime image
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
ENV TZ=UTC
# copy jar from build stage
COPY --from=build /workspace/target/nursery-0.0.1-SNAPSHOT.jar app.jar
# create folder for bills
RUN mkdir -p /app/bills
VOLUME /app/bills
EXPOSE 8080
ENTRYPOINT ["sh","-c","java -Djava.security.egd=file:/dev/./urandom -jar /app/app.jar"]