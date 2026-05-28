#!/bin/bash
# Build the Maven project (skip tests for faster dev)
mvn clean package -DskipTests
# Start Docker containers, building images if necessary
docker-compose up --build -d
# Show Jetty logs (Ctrl+C to exit)
echo "Jetty container logs:"
docker logs -f tallerwebi-jetty
