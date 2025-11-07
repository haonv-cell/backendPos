-- ============================================
-- Script: Add Provider Data Integrity Constraints
-- Purpose: Ensure data consistency for authentication providers
-- ============================================

-- 1. Check current invalid data before adding constraints
SELECT 
    id, 
    email, 
    provider, 
    provider_id,
    password_hash,
    CASE 
        WHEN provider = 'local' AND provider_id IS NOT NULL 
            THEN '⚠️ LOCAL account should not have provider_id'
        WHEN provider IN ('google', 'facebook') AND provider_id IS NULL 
            THEN '⚠️ OAuth account missing provider_id'
        WHEN provider IN ('google', 'facebook') AND password_hash IS NOT NULL 
            THEN '⚠️ OAuth account should not have password'
        WHEN provider = 'local' AND password_hash IS NULL 
            THEN '⚠️ LOCAL account missing password'
        ELSE '✅ OK'
    END as data_status
FROM users
WHERE 
    (provider = 'local' AND provider_id IS NOT NULL)
    OR (provider IN ('google', 'facebook') AND provider_id IS NULL)
    OR (provider IN ('google', 'facebook') AND password_hash IS NOT NULL)
    OR (provider = 'local' AND password_hash IS NULL);

-- 2. Fix invalid data before adding constraints

-- Fix 1: Remove provider_id from LOCAL accounts
UPDATE users 
SET provider_id = NULL,
    updated_at = CURRENT_TIMESTAMP
WHERE provider = 'local' 
  AND provider_id IS NOT NULL;

-- Fix 2: Convert OAuth accounts without provider_id to LOCAL (if they have password)
UPDATE users 
SET provider = 'local',
    updated_at = CURRENT_TIMESTAMP
WHERE provider IN ('google', 'facebook') 
  AND provider_id IS NULL 
  AND password_hash IS NOT NULL;

-- Fix 3: Mark invalid OAuth accounts (no provider_id, no password) as invalid
UPDATE users 
SET status = 'invalid_data',
    updated_at = CURRENT_TIMESTAMP
WHERE provider IN ('google', 'facebook') 
  AND provider_id IS NULL 
  AND password_hash IS NULL;

-- Fix 4: Mark invalid LOCAL accounts (no password) as invalid
UPDATE users 
SET status = 'invalid_data',
    updated_at = CURRENT_TIMESTAMP
WHERE provider = 'local' 
  AND password_hash IS NULL;

-- 3. Add constraint: OAuth accounts must have provider_id
ALTER TABLE users 
ADD CONSTRAINT check_oauth_provider_id 
CHECK (
    (LOWER(provider) = 'local' AND provider_id IS NULL) 
    OR 
    (LOWER(provider) IN ('google', 'facebook') AND provider_id IS NOT NULL)
);

-- 4. Add constraint: LOCAL accounts must have password_hash
-- Note: This is optional as OAuth accounts legitimately don't have passwords
-- Uncomment if you want to enforce this rule
-- ALTER TABLE users 
-- ADD CONSTRAINT check_local_password 
-- CHECK (
--     (LOWER(provider) = 'local' AND password_hash IS NOT NULL) 
--     OR 
--     (LOWER(provider) IN ('google', 'facebook'))
-- );

-- 5. Verify constraints were added successfully
SELECT 
    conname AS constraint_name,
    contype AS constraint_type,
    pg_get_constraintdef(oid) AS constraint_definition
FROM pg_constraint
WHERE conrelid = 'users'::regclass
  AND conname LIKE 'check_%';

-- 6. Test constraints by trying to insert invalid data (should fail)

-- Test 1: Try to insert OAuth account without provider_id (should fail)
-- INSERT INTO users (code, name, email, provider, provider_id, role, status)
-- VALUES ('TEST001', 'Test User', 'test@example.com', 'google', NULL, 'customer', 'active');
-- Expected: ERROR: new row for relation "users" violates check constraint "check_oauth_provider_id"

-- Test 2: Try to insert LOCAL account with provider_id (should fail)
-- INSERT INTO users (code, name, email, provider, provider_id, password_hash, role, status)
-- VALUES ('TEST002', 'Test User 2', 'test2@example.com', 'local', '123456', 'hashed_password', 'customer', 'active');
-- Expected: ERROR: new row for relation "users" violates check constraint "check_oauth_provider_id"

-- 7. Query to find users with invalid_data status (for manual review)
SELECT 
    id,
    code,
    email,
    name,
    provider,
    provider_id,
    CASE 
        WHEN password_hash IS NULL THEN 'No password'
        ELSE 'Has password'
    END as password_status,
    status,
    created_at
FROM users
WHERE status = 'invalid_data'
ORDER BY created_at DESC;

-- 8. Clean up invalid_data users (CAREFUL! Review first!)
-- Option 1: Delete invalid users (use with caution)
-- DELETE FROM users WHERE status = 'invalid_data';

-- Option 2: Keep for manual review
-- SELECT COUNT(*) as invalid_users_count FROM users WHERE status = 'invalid_data';

-- ============================================
-- Summary of Changes:
-- ============================================
-- 1. ✅ Fixed LOCAL accounts with provider_id
-- 2. ✅ Converted fixable OAuth accounts to LOCAL
-- 3. ✅ Marked unfixable accounts as invalid_data
-- 4. ✅ Added constraint: OAuth must have provider_id
-- 5. ✅ Added constraint: LOCAL must not have provider_id
-- ============================================

-- Final verification query
SELECT 
    provider,
    COUNT(*) as total_users,
    COUNT(CASE WHEN provider_id IS NOT NULL THEN 1 END) as with_provider_id,
    COUNT(CASE WHEN provider_id IS NULL THEN 1 END) as without_provider_id,
    COUNT(CASE WHEN password_hash IS NOT NULL THEN 1 END) as with_password,
    COUNT(CASE WHEN password_hash IS NULL THEN 1 END) as without_password,
    COUNT(CASE WHEN status = 'invalid_data' THEN 1 END) as invalid_count
FROM users
GROUP BY provider
ORDER BY provider;

