-- Fix decimal columns to match JPA BigDecimal mapping
-- The columns are already DECIMAL which is correct for BigDecimal
-- No changes needed for actual_hours and estimated_hours as they're already DECIMAL(10,2)

-- Add any missing columns or fix data types if needed
-- This migration is a placeholder to ensure schema version is updated
SELECT 1;