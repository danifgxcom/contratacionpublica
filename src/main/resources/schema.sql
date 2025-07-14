-- Create contract_types table if it doesn't exist
CREATE TABLE IF NOT EXISTS contract_types (
    code VARCHAR(10) PRIMARY KEY,
    description VARCHAR(100) NOT NULL,
    is_known BOOLEAN NOT NULL
);

-- Add a comment to the table
COMMENT ON TABLE contract_types IS 'Table for mapping contract type codes to their descriptions';