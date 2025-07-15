# Atom Importer

A standalone Spring Batch application for importing atom files into a database.

## Description

This application processes atom files containing contract information and imports them into a PostgreSQL database. It is a standalone application that uses Spring Batch without Spring Boot.

## Features

- Reads atom files from a specified directory
- Parses the atom files using Jackson XML
- Extracts contract information from the atom entries
- Saves the contracts to a PostgreSQL database
- Tracks processed files to avoid duplicate processing

## Requirements

- Java 11 or higher
- Maven
- PostgreSQL database

## Configuration

The application is configured using the `application.properties` file in the `src/main/resources` directory. The following properties can be configured:

- `spring.datasource.url`: The JDBC URL of the database
- `spring.datasource.username`: The database username
- `spring.datasource.password`: The database password
- `spring.datasource.driver-class-name`: The JDBC driver class name
- `app.file.input-directory`: The directory containing the atom files to process
- `app.file.processed-directory`: The directory where processed files will be moved

## Building

To build the application, run the following command:

```bash
mvn clean package
```

This will create a JAR file with all dependencies in the `target` directory.

## Running

To run the application, use the following command:

```bash
java -jar target/atom-importer-1.0.0-jar-with-dependencies.jar
```

## Database Schema

The application creates the following tables in the database:

### contracts

- `id`: UUID (primary key)
- `external_id`: VARCHAR(255) (unique)
- `title`: TEXT
- `summary`: TEXT
- `updated_at`: TIMESTAMP
- `imported_at`: TIMESTAMP
- `link`: TEXT
- `source_file`: VARCHAR(255)
- `folder_id`: VARCHAR(255)
- `status`: VARCHAR(50)
- `type_code`: VARCHAR(50)
- `subtype_code`: VARCHAR(50)
- `estimated_amount`: DOUBLE PRECISION
- `total_amount`: DOUBLE PRECISION
- `tax_exclusive_amount`: DOUBLE PRECISION
- `currency`: VARCHAR(10)
- `cpv_code`: VARCHAR(50)
- `country_subentity`: VARCHAR(255)
- `nuts_code`: VARCHAR(50)
- `contracting_party_name`: VARCHAR(255)
- `contracting_party_id`: VARCHAR(255)

### processed_files

- `id`: UUID (primary key)
- `file_name`: VARCHAR(255) (unique)
- `file_path`: VARCHAR(255)
- `contracts_processed`: INTEGER
- `processed_at`: TIMESTAMP
- `status`: VARCHAR(50)

## Troubleshooting

### CREATE_TIME column issue

If you encounter an error like the following:

```
ERROR: null value in column "create_time" of relation "batch_step_execution" violates not-null constraint
```

This is due to a mismatch between the Spring Batch schema and the version of Spring Batch being used. The application includes a fix for this issue by:

1. Modifying the `schema-postgresql.sql` file to set a default value for the `CREATE_TIME` column:
   ```sql
   CREATE_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   ```

2. Adding code to drop and recreate the `BATCH_STEP_EXECUTION` table with the new schema if it already exists.

This ensures that the `CREATE_TIME` column is properly initialized with the current timestamp when Spring Batch inserts records into the `BATCH_STEP_EXECUTION` table.
