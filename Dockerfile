# Use Maven to build the app
FROM maven:3.8.7-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Use OpenJDK for running the app
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
# Use the following command to build the Docker image
# docker build -t fileupload-app .
# Use the following command to run the Docker container
# docker run -p 8080:8080 fileupload-app
# Use the following command to run the Docker container in detached mode
# docker run -d -p 8080:8080 fileupload-app
# Use the following command to run the Docker container with a volume for file uploads
# docker run -d -p 8080:8080 -v /path/to/uploads:/app/uploads fileupload-app
# Use the following command to run the Docker container with a specific environment variable
# docker run -d -p 8080:8080 -e UPLOAD_DIR=/app/uploads fileupload-app
# Use the following command to run the Docker container with a specific network
# docker run -d -p 8080:8080 --network my-network fileupload-app
# Use the following command to run the Docker container with a specific name
# docker run -d -p 8080:8080 --name fileupload-container fileupload-app
# Use the following command to run the Docker container with a specific restart policy
# docker run -d -p 8080:8080 --restart unless-stopped fileupload-app
# Use the following command to run the Docker container with a specific user
# docker run -d -p 8080:8080 --user 1000:1000 fileupload-app
# Use the following command to run the Docker container with a specific memory limit
# docker run -d -p 8080:8080 --memory 512m fileupload-app
# Use the following command to run the Docker container with a specific CPU limit
# docker run -d -p 8080:8080 --cpus 1 fileupload-app
# Use the following command to run the Docker container with a specific log driver
# docker run -d -p 8080:8080 --log-driver json-file fileupload-app
# Use the following command to run the Docker container with a specific log options
# docker run -d -p 8080:8080 --log-opt max-size=10m --log-opt max-file=3 fileupload-app
# Use the following command to run the Docker container with a specific health check
# docker run -d -p 8080:8080 --health-cmd="curl --fail http://localhost:8080/health || exit 1" --health-interval=30s --health-timeout=10s --health-retries=3 fileupload-app
# Use the following command to run the Docker container with a specific environment file
# docker run -d -p 8080:8080 --env-file .env fileupload-app
# Use the following command to run the Docker container with a specific build argument
# docker build --build-arg UPLOAD_DIR=/app/uploads -t fileupload-app .
# Use the following command to run the Docker container with a specific build context
# docker build -f Dockerfile -t fileupload-app .
# Use the following command to run the Docker container with a specific target
# docker build --target build -t fileupload-app-build .
	
# Use the following command to run the Docker container with a specific platform
