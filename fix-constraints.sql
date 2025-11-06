-- Fix role and provider constraints to accept both uppercase and lowercase

-- Drop existing constraints
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_provider_check;

-- Add new constraints that accept both cases
ALTER TABLE users ADD CONSTRAINT users_role_check 
    CHECK (LOWER(role) IN ('admin', 'biller', 'supplier', 'store_owner', 'customer'));

ALTER TABLE users ADD CONSTRAINT users_provider_check 
    CHECK (LOWER(provider) IN ('local', 'google', 'facebook'));

