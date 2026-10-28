package com.paysentry.persistence;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AuditLogRepository {

    private final JdbcTemplate jdbc;

    public AuditLogRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void insert(String requestJson, String rulesFired, String decision, double score) {
        jdbc.update(
                """
                INSERT INTO audit_log (request_json, rules_fired, decision, score)
                VALUES (?, ?, ?, ?)
                """,
                requestJson,
                rulesFired,
                decision,
                score);
    }
}
