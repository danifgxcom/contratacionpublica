package com.danifgx.atomimporter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;

/**
 * Data Access Object for Contract entities.
 * This class handles database operations for contracts.
 */
@Component
public class ContractDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ContractDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        initializeDatabase();
    }

    /**
     * Initialize the database by creating the necessary tables if they don't exist.
     */
    private void initializeDatabase() {
        try {
            // Create the contracts table if it doesn't exist
            jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS contracts (" +
                "id UUID PRIMARY KEY, " +
                "external_id VARCHAR(255) UNIQUE, " +
                "title TEXT, " +
                "summary TEXT, " +
                "updated_at TIMESTAMP, " +
                "imported_at TIMESTAMP, " +
                "link TEXT, " +
                "source_file VARCHAR(255), " +
                "source VARCHAR(50), " +
                "folder_id VARCHAR(255), " +
                "status VARCHAR(50), " +
                "type_code VARCHAR(50), " +
                "subtype_code VARCHAR(50), " +
                "estimated_amount DOUBLE PRECISION, " +
                "total_amount DOUBLE PRECISION, " +
                "tax_exclusive_amount DOUBLE PRECISION, " +
                "currency VARCHAR(10), " +
                "cpv_code VARCHAR(50), " +
                "country_subentity VARCHAR(255), " +
                "nuts_code VARCHAR(50), " +
                "contracting_party_name VARCHAR(255), " +
                "contracting_party_id VARCHAR(255)" +
                ")"
            );

            // Create the processed_files table if it doesn't exist
            jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS processed_files (" +
                "id UUID PRIMARY KEY, " +
                "file_name VARCHAR(255) UNIQUE, " +
                "file_path VARCHAR(255), " +
                "contracts_processed INTEGER, " +
                "processed_at TIMESTAMP, " +
                "status VARCHAR(50)" +
                ")"
            );

            // Create indexes if they don't exist
            try {
                // Index on contracting_party_name (organismo)
                jdbcTemplate.execute(
                    "CREATE INDEX IF NOT EXISTS idx_organismo ON contracts (contracting_party_name)"
                );

                // Index on updated_at (fecha_publicacion)
                jdbcTemplate.execute(
                    "CREATE INDEX IF NOT EXISTS idx_fecha ON contracts (updated_at)"
                );

                // GIN index on title using Spanish text search vectors
                jdbcTemplate.execute(
                    "CREATE INDEX IF NOT EXISTS idx_titulo_gin ON contracts USING GIN (to_tsvector('spanish', title))"
                );

                System.out.println("Database indexes created successfully");
            } catch (Exception indexEx) {
                System.err.println("Error creating indexes: " + indexEx.getMessage());
                indexEx.printStackTrace();
            }

            System.out.println("Database tables initialized successfully");
        } catch (Exception e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Save a list of contracts to the database.
     *
     * @param contracts the contracts to save
     */
    @Transactional
    public void saveContracts(List<Contract> contracts) {
        try {
            System.out.println("Saving " + contracts.size() + " contracts to database");

            String sql = "INSERT INTO contracts (id, external_id, title, summary, updated_at, imported_at, link, " +
                    "source_file, source, folder_id, status, type_code, subtype_code, estimated_amount, total_amount, " +
                    "tax_exclusive_amount, currency, cpv_code, country_subentity, nuts_code, " +
                    "contracting_party_name, contracting_party_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON CONFLICT (external_id) DO NOTHING";

            jdbcTemplate.batchUpdate(sql, contracts, contracts.size(), (PreparedStatement ps, Contract contract) -> {
                ps.setObject(1, contract.getId());
                ps.setString(2, contract.getExternalId());
                ps.setString(3, contract.getTitle());
                ps.setString(4, contract.getSummary());
                ps.setTimestamp(5, contract.getUpdatedAt() != null ? Timestamp.valueOf(contract.getUpdatedAt()) : null);
                ps.setTimestamp(6, contract.getImportedAt() != null ? Timestamp.valueOf(contract.getImportedAt()) : null);
                ps.setString(7, contract.getLink());
                ps.setString(8, contract.getSourceFile());
                ps.setString(9, contract.getSource());
                ps.setString(10, contract.getFolderId());
                ps.setString(11, contract.getStatus());
                ps.setString(12, contract.getTypeCode());
                ps.setString(13, contract.getSubtypeCode());
                ps.setObject(14, contract.getEstimatedAmount());
                ps.setObject(15, contract.getTotalAmount());
                ps.setObject(16, contract.getTaxExclusiveAmount());
                ps.setString(17, contract.getCurrency());
                ps.setString(18, contract.getCpvCode());
                ps.setString(19, contract.getCountrySubentity());
                ps.setString(20, contract.getNutsCode());
                ps.setString(21, contract.getContractingPartyName());
                ps.setString(22, contract.getContractingPartyId());
            });

            // Record the processed file
            String fileName = contracts.get(0).getSourceFile();
            recordProcessedFile(fileName, contracts.size());

            System.out.println("Successfully saved " + contracts.size() + " contracts to database");
        } catch (Exception e) {
            System.err.println("Error saving contracts to database: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error saving contracts to database", e);
        }
    }

    /**
     * Record a processed file in the database.
     *
     * @param fileName the name of the processed file
     * @param contractsProcessed the number of contracts processed from the file
     */
    private void recordProcessedFile(String fileName, int contractsProcessed) {
        try {
            String sql = "INSERT INTO processed_files (id, file_name, file_path, contracts_processed, processed_at, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?) " +
                    "ON CONFLICT (file_name) DO NOTHING";

            jdbcTemplate.update(sql, 
                    java.util.UUID.randomUUID(),
                    fileName,
                    fileName,
                    contractsProcessed,
                    Timestamp.valueOf(java.time.LocalDateTime.now()),
                    "COMPLETED"
            );

            System.out.println("Recorded processed file: " + fileName);
        } catch (Exception e) {
            System.err.println("Error recording processed file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
