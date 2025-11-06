// package com.example.pos.util;

// import org.springframework.boot.CommandLineRunner;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.jdbc.core.JdbcTemplate;

// /**
//  * Utility to fix database constraints on application startup
//  * This will update the constraints to accept both uppercase and lowercase values
//  * 
//  * IMPORTANT: After running once successfully, you can disable this by commenting out the @Configuration annotation
//  */
// @Configuration
// public class DatabaseConstraintFixer {

//     @Bean
//     public CommandLineRunner fixConstraints(JdbcTemplate jdbcTemplate) {
//         return args -> {
//             try {
//                 System.out.println("=".repeat(80));
//                 System.out.println("🔧 FIXING DATABASE CONSTRAINTS...");
//                 System.out.println("=".repeat(80));
                
//                 // Drop existing constraints
//                 System.out.println("📌 Dropping old constraints...");
//                 jdbcTemplate.execute("ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check");
//                 jdbcTemplate.execute("ALTER TABLE users DROP CONSTRAINT IF EXISTS users_provider_check");
//                 System.out.println("✅ Old constraints dropped");
                
//                 // Add new constraints that accept both cases
//                 System.out.println("📌 Adding new constraints...");
//                 jdbcTemplate.execute(
//                     "ALTER TABLE users ADD CONSTRAINT users_role_check " +
//                     "CHECK (LOWER(role) IN ('admin', 'biller', 'supplier', 'store_owner', 'customer'))"
//                 );
//                 jdbcTemplate.execute(
//                     "ALTER TABLE users ADD CONSTRAINT users_provider_check " +
//                     "CHECK (LOWER(provider) IN ('local', 'google', 'facebook'))"
//                 );
//                 System.out.println("✅ New constraints added");
                
//                 System.out.println("=".repeat(80));
//                 System.out.println("✅ DATABASE CONSTRAINTS FIXED SUCCESSFULLY!");
//                 System.out.println("=".repeat(80));
//                 System.out.println("ℹ️  You can now disable this fixer by commenting out @Configuration");
//                 System.out.println("   in DatabaseConstraintFixer.java");
//                 System.out.println("=".repeat(80));
                
//             } catch (Exception e) {
//                 System.err.println("=".repeat(80));
//                 System.err.println("❌ FAILED TO FIX CONSTRAINTS!");
//                 System.err.println("=".repeat(80));
//                 System.err.println("Error: " + e.getMessage());
//                 System.err.println("=".repeat(80));
//                 System.err.println("Please fix manually using pgAdmin:");
//                 System.err.println("1. Open pgAdmin and connect to 'pos' database");
//                 System.err.println("2. Run the SQL from fix-constraints.sql file");
//                 System.err.println("=".repeat(80));
//             }
//         };
//     }
// }

