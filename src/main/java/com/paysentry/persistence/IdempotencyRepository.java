package com.paysentry.persistence;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class IdempotencyRepository {

    private final JdbcTemplate jdbc;

    public IdempotencyRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<String> findResponseJson(String key) {
        try {
            String json = jdbc.queryForObject(
                    "SELECT response_json FROM idempotency_keys WHERE key = ?",
                    String.class,
                    key);
            return Optional.ofNullable(json);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    public void insert(String key, String responseJson) {
        jdbc.update(
                "INSERT INTO idempotency_keys (key, response_json) VALUES (?, ?)",
                key,
                responseJson);
    }
}
