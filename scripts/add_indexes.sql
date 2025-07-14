-- SQL script to add indexes to the contracts table

-- Index on contracting_party_name (organismo)
CREATE INDEX IF NOT EXISTS idx_organismo ON contracts (contracting_party_name);

-- Index on updated_at (fecha_publicacion)
CREATE INDEX IF NOT EXISTS idx_fecha ON contracts (updated_at);

-- GIN index on title using Spanish text search vectors
CREATE INDEX IF NOT EXISTS idx_titulo_gin ON contracts USING GIN (to_tsvector('spanish', title));

-- Output a message to confirm the indexes were created
DO $$
BEGIN
    RAISE NOTICE 'Indexes created successfully';
END $$;