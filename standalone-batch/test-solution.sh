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

# Check the exit code
exit_code=$?
if [ $exit_code -eq 0 ]; then
    echo "Application executed successfully with exit code 0."
else
    echo "Application execution failed with exit code $exit_code."
fi

exit $exit_code