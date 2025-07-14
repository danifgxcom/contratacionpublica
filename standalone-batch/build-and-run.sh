#!/bin/bash

# Build the application
echo "Building the application..."
cd "$(dirname "$0")"
mvn clean package

# Check if the build was successful
if [ $? -ne 0 ]; then
    echo "Build failed. Exiting."
    exit 1
fi

# Run the application
echo "Running the application..."
java -jar target/atom-importer-1.0.0-jar-with-dependencies.jar

# Check if the application ran successfully
if [ $? -ne 0 ]; then
    echo "Application execution failed."
    exit 1
else
    echo "Application executed successfully."
fi