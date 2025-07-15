package com.danifgx.atomimporter;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration class for Spring Batch.
 * This implementation processes atom files and imports them into a database.
 */
@Configuration
@EnableBatchProcessing
@ComponentScan(basePackages = "com.danifgx.atomimporter")
@PropertySource("classpath:application.properties")
public class BatchConfig {

    private final Environment environment;
    private JdbcTemplate jdbcTemplate;

    public BatchConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        return this.jdbcTemplate;
    }

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(environment.getProperty("spring.datasource.driver-class-name", "org.postgresql.Driver"));
        dataSource.setUrl(environment.getProperty("spring.datasource.url", "jdbc:postgresql://localhost:5432/contratacionpublica"));
        dataSource.setUsername(environment.getProperty("spring.datasource.username", "postgres"));
        dataSource.setPassword(environment.getProperty("spring.datasource.password", "postgres"));
        return dataSource;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new ResourcelessTransactionManager();
    }

    @Bean
    public JobRepository jobRepository() throws Exception {
        // Initialize the database schema
        DataSource dataSource = dataSource();
        initializeSchema(dataSource);

        // Create the job repository
        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(dataSource);
        factory.setTransactionManager(transactionManager());
        factory.setIsolationLevelForCreate("ISOLATION_SERIALIZABLE");
        factory.setTablePrefix("BATCH_");
        factory.setMaxVarCharLength(1000);
        factory.afterPropertiesSet();
        return factory.getObject();
    }

    /**
     * Initialize the database schema using our custom schema script.
     */
    private void initializeSchema(DataSource dataSource) {
        try {
            System.out.println("Initializing database schema...");

            // First check if the tables already exist
            if (tablesExist(dataSource)) {
                System.out.println("Batch tables already exist, checking for missing columns...");

                // Drop the BATCH_STEP_EXECUTION table to recreate it with the new schema
                org.springframework.jdbc.core.JdbcTemplate jdbcTemplate = new org.springframework.jdbc.core.JdbcTemplate(dataSource);
                try {
                    System.out.println("Dropping BATCH_STEP_EXECUTION table to recreate it with the new schema...");
                    jdbcTemplate.execute("DROP TABLE IF EXISTS BATCH_STEP_EXECUTION_CONTEXT");
                    jdbcTemplate.execute("DROP TABLE IF EXISTS BATCH_STEP_EXECUTION");
                    System.out.println("BATCH_STEP_EXECUTION table dropped successfully");

                    // Create the table with the new schema
                    System.out.println("Creating BATCH_STEP_EXECUTION table with the new schema...");
                    Resource schemaScript = new org.springframework.core.io.ClassPathResource("schema-postgresql.sql");
                    String schemaContent = org.springframework.util.StreamUtils.copyToString(
                        schemaScript.getInputStream(), java.nio.charset.StandardCharsets.UTF_8);

                    // Extract and execute only the BATCH_STEP_EXECUTION table creation part
                    String stepExecutionTableScript = extractTableScript(schemaContent, "BATCH_STEP_EXECUTION");
                    String stepExecutionContextTableScript = extractTableScript(schemaContent, "BATCH_STEP_EXECUTION_CONTEXT");

                    jdbcTemplate.execute(stepExecutionTableScript);
                    jdbcTemplate.execute(stepExecutionContextTableScript);

                    System.out.println("BATCH_STEP_EXECUTION table created successfully with the new schema");
                } catch (Exception e) {
                    System.err.println("Error recreating BATCH_STEP_EXECUTION table: " + e.getMessage());
                    e.printStackTrace();
                }

                addMissingColumns(dataSource);
            } else {
                System.out.println("Creating batch tables from schema script...");
                Resource schemaScript = new org.springframework.core.io.ClassPathResource("schema-postgresql.sql");

                org.springframework.jdbc.datasource.init.ResourceDatabasePopulator populator = 
                    new org.springframework.jdbc.datasource.init.ResourceDatabasePopulator();
                populator.setContinueOnError(true);
                populator.addScript(schemaScript);

                org.springframework.jdbc.datasource.init.DatabasePopulatorUtils.execute(populator, dataSource);
                System.out.println("Database schema initialized successfully");
            }
        } catch (Exception e) {
            System.err.println("Error initializing database schema: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Extract the CREATE TABLE script for a specific table from the schema script.
     */
    private String extractTableScript(String schemaContent, String tableName) {
        int startIndex = schemaContent.indexOf("CREATE TABLE " + tableName);
        int endIndex = schemaContent.indexOf(";", startIndex) + 1;
        return schemaContent.substring(startIndex, endIndex);
    }

    /**
     * Check if the batch tables already exist in the database.
     */
    private boolean tablesExist(DataSource dataSource) {
        try {
            org.springframework.jdbc.core.JdbcTemplate jdbcTemplate = new org.springframework.jdbc.core.JdbcTemplate(dataSource);
            String query = "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'batch_job_execution'";
            Integer count = jdbcTemplate.queryForObject(query, Integer.class);
            return count != null && count > 0;
        } catch (Exception e) {
            System.err.println("Error checking if tables exist: " + e.getMessage());
            return false;
        }
    }

    /**
     * Add any missing columns to the existing batch tables.
     */
    private void addMissingColumns(DataSource dataSource) {
        try {
            org.springframework.jdbc.core.JdbcTemplate jdbcTemplate = new org.springframework.jdbc.core.JdbcTemplate(dataSource);

            // Check if JOB_CONFIGURATION_LOCATION column exists in BATCH_JOB_EXECUTION
            String query = "SELECT COUNT(*) FROM information_schema.columns " +
                           "WHERE table_name = 'batch_job_execution' AND column_name = 'job_configuration_location'";
            Integer count = jdbcTemplate.queryForObject(query, Integer.class);

            if (count != null && count == 0) {
                System.out.println("Adding missing JOB_CONFIGURATION_LOCATION column to BATCH_JOB_EXECUTION table");
                jdbcTemplate.execute("ALTER TABLE BATCH_JOB_EXECUTION ADD COLUMN JOB_CONFIGURATION_LOCATION VARCHAR(2500)");
                System.out.println("Column added successfully");
            } else {
                System.out.println("JOB_CONFIGURATION_LOCATION column already exists");
            }

            // Check the structure of BATCH_JOB_EXECUTION_PARAMS table
            System.out.println("Checking BATCH_JOB_EXECUTION_PARAMS table structure...");

            // Get the column names from the table
            List<String> columnNames = jdbcTemplate.queryForList(
                "SELECT column_name FROM information_schema.columns WHERE table_name = 'batch_job_execution_params'",
                String.class
            );

            System.out.println("Existing columns in BATCH_JOB_EXECUTION_PARAMS: " + columnNames);

            // Check if the table has the expected columns
            if (!columnNames.contains("key_name")) {
                // The table might have a different column name for KEY_NAME
                if (columnNames.contains("parameter_name")) {
                    System.out.println("Found 'parameter_name' column instead of 'key_name'");

                    // Create a new table with the correct structure
                    System.out.println("Creating a new BATCH_JOB_EXECUTION_PARAMS table with the correct structure");

                    // Rename the existing table
                    jdbcTemplate.execute("ALTER TABLE BATCH_JOB_EXECUTION_PARAMS RENAME TO BATCH_JOB_EXECUTION_PARAMS_OLD");

                    // Create a new table with the correct structure
                    jdbcTemplate.execute(
                        "CREATE TABLE BATCH_JOB_EXECUTION_PARAMS (" +
                        "JOB_EXECUTION_ID BIGINT NOT NULL, " +
                        "TYPE_CD VARCHAR(6) NOT NULL, " +
                        "KEY_NAME VARCHAR(100) NOT NULL, " +
                        "STRING_VAL VARCHAR(250), " +
                        "DATE_VAL TIMESTAMP DEFAULT NULL, " +
                        "LONG_VAL BIGINT, " +
                        "DOUBLE_VAL DOUBLE PRECISION, " +
                        "IDENTIFYING CHAR(1) NOT NULL, " +
                        "constraint JOB_EXEC_PARAMS_FK foreign key (JOB_EXECUTION_ID) " +
                        "references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID))"
                    );

                    // Copy data from the old table to the new one, mapping column names
                    try {
                        jdbcTemplate.execute(
                            "INSERT INTO BATCH_JOB_EXECUTION_PARAMS " +
                            "(JOB_EXECUTION_ID, TYPE_CD, KEY_NAME, STRING_VAL, DATE_VAL, LONG_VAL, DOUBLE_VAL, IDENTIFYING) " +
                            "SELECT JOB_EXECUTION_ID, TYPE_CD, PARAMETER_NAME, STRING_VAL, DATE_VAL, LONG_VAL, DOUBLE_VAL, IDENTIFYING " +
                            "FROM BATCH_JOB_EXECUTION_PARAMS_OLD"
                        );
                        System.out.println("Data migrated successfully from old table to new table");
                    } catch (Exception e) {
                        System.out.println("Error migrating data, but continuing: " + e.getMessage());
                        // Continue even if data migration fails
                    }
                } else {
                    // Just create the table with the correct structure
                    System.out.println("Creating BATCH_JOB_EXECUTION_PARAMS table with the correct structure");
                    jdbcTemplate.execute(
                        "DROP TABLE IF EXISTS BATCH_JOB_EXECUTION_PARAMS"
                    );
                    jdbcTemplate.execute(
                        "CREATE TABLE BATCH_JOB_EXECUTION_PARAMS (" +
                        "JOB_EXECUTION_ID BIGINT NOT NULL, " +
                        "TYPE_CD VARCHAR(6) NOT NULL, " +
                        "KEY_NAME VARCHAR(100) NOT NULL, " +
                        "STRING_VAL VARCHAR(250), " +
                        "DATE_VAL TIMESTAMP DEFAULT NULL, " +
                        "LONG_VAL BIGINT, " +
                        "DOUBLE_VAL DOUBLE PRECISION, " +
                        "IDENTIFYING CHAR(1) NOT NULL, " +
                        "constraint JOB_EXEC_PARAMS_FK foreign key (JOB_EXECUTION_ID) " +
                        "references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID))"
                    );
                }
                System.out.println("BATCH_JOB_EXECUTION_PARAMS table updated successfully");
            } else {
                System.out.println("BATCH_JOB_EXECUTION_PARAMS table has the correct structure");
            }

            // Check the structure of BATCH_STEP_EXECUTION table
            System.out.println("Checking BATCH_STEP_EXECUTION table structure...");

            // Get the column names from the table
            List<String> stepExecColumns = jdbcTemplate.queryForList(
                "SELECT column_name FROM information_schema.columns WHERE table_name = 'batch_step_execution'",
                String.class
            );

            System.out.println("Existing columns in BATCH_STEP_EXECUTION: " + stepExecColumns);

            // Check if the CREATE_TIME column exists
            if (!stepExecColumns.contains("create_time")) {
                System.out.println("Adding missing CREATE_TIME column to BATCH_STEP_EXECUTION table");

                // Add the CREATE_TIME column
                jdbcTemplate.execute("ALTER TABLE BATCH_STEP_EXECUTION ADD COLUMN CREATE_TIME TIMESTAMP");

                // Update existing records to set CREATE_TIME to the same value as START_TIME or current time
                jdbcTemplate.execute(
                    "UPDATE BATCH_STEP_EXECUTION SET CREATE_TIME = " +
                    "CASE WHEN START_TIME IS NOT NULL THEN START_TIME ELSE NOW() END"
                );

                // Make the column NOT NULL
                jdbcTemplate.execute("ALTER TABLE BATCH_STEP_EXECUTION ALTER COLUMN CREATE_TIME SET NOT NULL");

                System.out.println("CREATE_TIME column added successfully to BATCH_STEP_EXECUTION table");
            } else {
                System.out.println("CREATE_TIME column already exists in BATCH_STEP_EXECUTION table");

                // Ensure that any existing records have a valid CREATE_TIME value
                jdbcTemplate.execute(
                    "UPDATE BATCH_STEP_EXECUTION SET CREATE_TIME = " +
                    "CASE WHEN CREATE_TIME IS NULL AND START_TIME IS NOT NULL THEN START_TIME " +
                    "WHEN CREATE_TIME IS NULL THEN NOW() " +
                    "ELSE CREATE_TIME END"
                );
            }

            // Verify that the column is set to NOT NULL
            String columnNullableQuery = "SELECT is_nullable FROM information_schema.columns " +
                                         "WHERE table_name = 'batch_step_execution' AND column_name = 'create_time'";
            String isNullable = jdbcTemplate.queryForObject(columnNullableQuery, String.class);

            if ("YES".equalsIgnoreCase(isNullable)) {
                System.out.println("Setting CREATE_TIME column to NOT NULL");
                jdbcTemplate.execute("ALTER TABLE BATCH_STEP_EXECUTION ALTER COLUMN CREATE_TIME SET NOT NULL");
            }
        } catch (Exception e) {
            System.err.println("Error adding missing columns: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Bean
    public JobBuilderFactory jobBuilderFactory(JobRepository jobRepository) {
        return new JobBuilderFactory(jobRepository);
    }

    @Bean
    public StepBuilderFactory stepBuilderFactory(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilderFactory(jobRepository, transactionManager);
    }

    @Bean
    public Job processAtomFilesJob(JobBuilderFactory jobBuilderFactory, Step processAtomFilesStep) {
        return jobBuilderFactory.get("processAtomFilesJob")
                .incrementer(new RunIdIncrementer())
                .start(processAtomFilesStep)
                .build();
    }

    @Bean
    public Step processAtomFilesStep(StepBuilderFactory stepBuilderFactory,
                                    ItemReader<File> fileItemReader, 
                                    ItemProcessor<File, List<Contract>> atomFileProcessor,
                                    ItemWriter<List<Contract>> contractWriter) {
        return stepBuilderFactory.get("processAtomFilesStep")
                .<File, List<Contract>>chunk(1)
                .reader(fileItemReader)
                .processor(atomFileProcessor)
                .writer(contractWriter)
                .build();
    }

    @Bean
    public ItemReader<File> fileItemReader() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            String inputDirectory = environment.getProperty("app.file.input-directory", "./licitaciones");

            // Use a general pattern to find all .atom files in the input directory and its subdirectories
            String pattern = "file:" + inputDirectory + "/**/*.atom";
            System.out.println("Looking for files with pattern: " + pattern);
            Resource[] resources = resolver.getResources(pattern);
            System.out.println("Found " + resources.length + " resources with pattern");

            // Get list of already processed files from database
            List<String> processedFiles = getProcessedFileNames();
            System.out.println("Found " + processedFiles.size() + " already processed files");

            List<File> files = new ArrayList<>();
            for (Resource resource : resources) {
                File file = resource.getFile();
                // Check if file has already been processed
                if (processedFiles.contains(file.getName())) {
                    System.out.println("Skipping already processed file: " + file.getName());
                } else {
                    System.out.println("Adding file to process: " + file.getAbsolutePath());
                    files.add(file);
                }
            }

            System.out.println("Total files to process: " + files.size());
            return new ListItemReader<>(files);
        } catch (Exception e) {
            System.err.println("Error creating atom file reader: " + e.getMessage());
            e.printStackTrace();
            return new ListItemReader<>(new ArrayList<>());
        }
    }

    /**
     * Get a list of already processed file names from the database.
     * 
     * @return a list of processed file names
     */
    private List<String> getProcessedFileNames() {
        try {
            String sql = "SELECT file_name FROM processed_files";
            return jdbcTemplate.queryForList(sql, String.class);
        } catch (Exception e) {
            System.err.println("Error getting processed file names: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Bean
    public ItemProcessor<File, List<Contract>> atomFileProcessor(AtomFileReader atomFileReader) {
        return file -> {
            try {
                System.out.println("Processing file: " + file.getAbsolutePath());
                AtomFeed feed = atomFileReader.readAtomFile(file);

                if (feed != null && feed.getEntries() != null) {
                    System.out.println("File " + file.getName() + " has " + feed.getEntries().size() + " entries");
                    List<Contract> contracts = new ArrayList<>();

                    for (AtomEntry entry : feed.getEntries()) {
                        System.out.println("Processing entry with ID: " + entry.getId());
                        Contract contract = convertEntryToContract(entry, file.getName());
                        contracts.add(contract);
                    }

                    System.out.println("Processed file " + file.getName() + " with " + contracts.size() + " contracts");
                    return contracts;
                } else {
                    System.out.println("File " + file.getName() + " has no entries or could not be parsed");
                }

                return new ArrayList<>();
            } catch (Exception e) {
                System.err.println("Error processing file " + file.getName() + ": " + e.getMessage());
                e.printStackTrace();
                return new ArrayList<>();
            }
        };
    }

    @Bean
    public ItemWriter<List<Contract>> contractWriter(ContractDao contractDao) {
        return items -> {
            System.out.println("Contract writer called with " + items.size() + " items");
            for (List<Contract> contracts : items) {
                System.out.println("Processing contract list with " + contracts.size() + " contracts");
                if (!contracts.isEmpty()) {
                    String fileName = contracts.get(0).getSourceFile();
                    System.out.println("Saving contracts from file " + fileName);
                    contractDao.saveContracts(contracts);
                    System.out.println("Processed file " + fileName + " with " + contracts.size() + " contracts");
                } else {
                    System.out.println("Contract list is empty, skipping");
                }
            }
        };
    }

    /**
     * Convert an AtomEntry to a Contract.
     */
    private Contract convertEntryToContract(AtomEntry entry, String sourceFileName) {
        // Map the entry to a contract
        Contract contract = new Contract();
        contract.setExternalId(entry.getId());

        // Truncate title if it exceeds 1000 characters
        String title = entry.getTitle();
        if (title != null && title.length() > 1000) {
            title = title.substring(0, 1000);
            System.out.println("Truncated title for entry ID: " + entry.getId());
        }
        contract.setTitle(title);

        // Truncate summary if it exceeds 1000 characters
        String summary = entry.getSummary();
        if (summary != null && summary.length() > 1000) {
            summary = summary.substring(0, 1000);
            System.out.println("Truncated summary for entry ID: " + entry.getId());
        }
        contract.setSummary(summary);

        contract.setUpdatedAt(entry.getUpdated() != null ? entry.getUpdated().toLocalDateTime() : null);

        // Truncate link if it exceeds 1000 characters
        String link = entry.getLink() != null ? entry.getLink().getHref() : null;
        if (link != null && link.length() > 1000) {
            link = link.substring(0, 1000);
            System.out.println("Truncated link for entry ID: " + entry.getId());
        }
        contract.setLink(link);

        // Truncate sourceFile if it exceeds 1000 characters
        if (sourceFileName != null && sourceFileName.length() > 1000) {
            sourceFileName = sourceFileName.substring(0, 1000);
            System.out.println("Truncated source file name for entry ID: " + entry.getId());
        }
        contract.setSourceFile(sourceFileName);

        // Set the source field based on the file name
        if (sourceFileName != null) {
            if (sourceFileName.toLowerCase().contains("perfiles")) {
                contract.setSource("perfiles");
            } else if (sourceFileName.toLowerCase().contains("agregadas")) {
                contract.setSource("agregadas");
            } else {
                System.out.println("Warning: Could not determine source from file name: " + sourceFileName);
                contract.setSource("unknown");
            }
        }

        // Map contract folder status if available
        if (entry.getContractFolderStatus() != null) {
            AtomEntry.ContractFolderStatus status = entry.getContractFolderStatus();

            // Truncate folderId if it exceeds 1000 characters
            String folderId = status.getContractFolderId();
            if (folderId != null && folderId.length() > 1000) {
                folderId = folderId.substring(0, 1000);
                System.out.println("Truncated folder ID for entry ID: " + entry.getId());
            }
            contract.setFolderId(folderId);

            // Truncate status if it exceeds 1000 characters
            String statusCode = status.getContractFolderStatusCode();
            if (statusCode != null && statusCode.length() > 1000) {
                statusCode = statusCode.substring(0, 1000);
                System.out.println("Truncated status code for entry ID: " + entry.getId());
            }
            contract.setStatus(statusCode);

            // Map procurement project if available
            if (status.getProcurementProject() != null) {
                AtomEntry.ProcurementProject project = status.getProcurementProject();

                // Truncate typeCode if it exceeds 1000 characters
                String typeCode = project.getTypeCode();
                if (typeCode != null && typeCode.length() > 1000) {
                    typeCode = typeCode.substring(0, 1000);
                    System.out.println("Truncated type code for entry ID: " + entry.getId());
                }
                contract.setTypeCode(typeCode);

                // Truncate subtypeCode if it exceeds 1000 characters
                String subtypeCode = project.getSubTypeCode();
                if (subtypeCode != null && subtypeCode.length() > 1000) {
                    subtypeCode = subtypeCode.substring(0, 1000);
                    System.out.println("Truncated subtype code for entry ID: " + entry.getId());
                }
                contract.setSubtypeCode(subtypeCode);

                // Map budget amount if available
                if (project.getBudgetAmount() != null) {
                    AtomEntry.BudgetAmount budget = project.getBudgetAmount();
                    contract.setEstimatedAmount(parseAmount(budget.getEstimatedOverallContractAmount()));
                    contract.setTotalAmount(parseAmount(budget.getTotalAmount()));
                    contract.setTaxExclusiveAmount(parseAmount(budget.getTaxExclusiveAmount()));
                    contract.setCurrency("EUR"); // Assuming EUR for all contracts
                }

                // Map CPV code if available
                if (project.getRequiredCommodityClassification() != null) {
                    String cpvCode = project.getRequiredCommodityClassification().getItemClassificationCode();
                    if (cpvCode != null && cpvCode.length() > 1000) {
                        cpvCode = cpvCode.substring(0, 1000);
                        System.out.println("Truncated CPV code for entry ID: " + entry.getId());
                    }
                    contract.setCpvCode(cpvCode);
                }

                // Map location if available
                if (project.getRealizedLocation() != null) {
                    String countrySubentity = project.getRealizedLocation().getCountrySubentity();
                    if (countrySubentity != null && countrySubentity.length() > 1000) {
                        countrySubentity = countrySubentity.substring(0, 1000);
                        System.out.println("Truncated country subentity for entry ID: " + entry.getId());
                    }
                    contract.setCountrySubentity(countrySubentity);

                    String nutsCode = project.getRealizedLocation().getCountrySubentityCode();
                    if (nutsCode != null && nutsCode.length() > 1000) {
                        nutsCode = nutsCode.substring(0, 1000);
                        System.out.println("Truncated NUTS code for entry ID: " + entry.getId());
                    }
                    contract.setNutsCode(nutsCode);
                }
            }

            // Map contracting party if available
            if (status.getLocatedContractingParty() != null && 
                status.getLocatedContractingParty().getParty() != null) {
                AtomEntry.Party party = status.getLocatedContractingParty().getParty();

                if (party.getPartyName() != null) {
                    String partyName = party.getPartyName().getName();
                    if (partyName != null && partyName.length() > 1000) {
                        partyName = partyName.substring(0, 1000);
                        System.out.println("Truncated contracting party name for entry ID: " + entry.getId());
                    }
                    contract.setContractingPartyName(partyName);
                }

                if (party.getPartyIdentification() != null) {
                    String partyId = party.getPartyIdentification().getId();
                    if (partyId != null && partyId.length() > 1000) {
                        partyId = partyId.substring(0, 1000);
                        System.out.println("Truncated contracting party ID for entry ID: " + entry.getId());
                    }
                    contract.setContractingPartyId(partyId);
                }
            }
        }

        return contract;
    }

    /**
     * Parse an amount string to a double.
     */
    private Double parseAmount(String amount) {
        if (amount == null || amount.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(amount);
        } catch (NumberFormatException e) {
            System.out.println("Error parsing amount: " + amount);
            return null;
        }
    }
}
