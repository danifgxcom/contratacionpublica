-- Test to verify ascending sort is working correctly
-- This mimics the COALESCE logic used in the backend

-- First, let's see some sample data with amounts
SELECT 
    id,
    title,
    contracting_party_name,
    updated_at,
    total_amount,
    tax_exclusive_amount,
    estimated_amount,
    COALESCE(total_amount, tax_exclusive_amount, estimated_amount) as effective_amount
FROM contracts 
WHERE COALESCE(total_amount, tax_exclusive_amount, estimated_amount) IS NOT NULL
ORDER BY COALESCE(total_amount, tax_exclusive_amount, estimated_amount) ASC NULLS LAST
LIMIT 10;

-- Let's also test descending to compare
SELECT 
    id,
    title,
    contracting_party_name,
    updated_at,
    total_amount,
    tax_exclusive_amount,
    estimated_amount,
    COALESCE(total_amount, tax_exclusive_amount, estimated_amount) as effective_amount
FROM contracts 
WHERE COALESCE(total_amount, tax_exclusive_amount, estimated_amount) IS NOT NULL
ORDER BY COALESCE(total_amount, tax_exclusive_amount, estimated_amount) DESC NULLS LAST
LIMIT 10;