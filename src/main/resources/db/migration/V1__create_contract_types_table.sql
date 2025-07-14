-- Create contract_types table
CREATE TABLE IF NOT EXISTS contract_types (
    code VARCHAR(10) PRIMARY KEY,
    description VARCHAR(100) NOT NULL,
    is_known BOOLEAN NOT NULL
);

-- Insert known contract types
INSERT INTO contract_types (code, description, is_known) VALUES
('01', 'Obras', true),
('02', 'Suministros', true),
('03', 'Servicios', true),
('04', 'Concesión de obras', true),
('05', 'Concesión de servicios', true),
('06', 'Contratos mixtos', true),
('21', 'Administrativo especial', true),
('22', 'Sujeto a regulación armonizada', true),
('31', 'Contrato subvencionado', true),
('32', 'Derivado de acuerdo marco', true),
('50', 'Asociación público-privada', true),
('99', 'Otro o sin especificar', true),
('999', 'No clasificado / error / legacy', true);

-- Add a comment to the table
COMMENT ON TABLE contract_types IS 'Table for mapping contract type codes to their descriptions';