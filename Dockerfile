FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/*.jar wheelbase-server.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/wheelbase-server.jar"]