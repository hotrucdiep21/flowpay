package com.flowpay.support;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@FlowPayIntegrationTest
@ActiveProfiles("jpa")
class PostgresContainerSmokeTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void should_start_postgres_and_run_flyway_migrations() {
        String databaseVersion = jdbcTemplate.queryForObject(
                "SELECT version()",
                String.class
        );

        Integer walletTableCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_schema = 'public'
                  AND table_name = 'wallets'
                """,
                Integer.class
        );

        Integer transferTableCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_schema = 'public'
                  AND table_name = 'transfers'
                """,
                Integer.class
        );

        assertTrue(databaseVersion.contains("PostgreSQL"));
        assertEquals(1, walletTableCount);
        assertEquals(1, transferTableCount);
    }
}