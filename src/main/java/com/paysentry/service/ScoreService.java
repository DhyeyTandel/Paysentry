package com.paysentry.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paysentry.api.ScoreRequest;
import com.paysentry.api.ScoreResponse;
import com.paysentry.persistence.AuditLogRepository;
import com.paysentry.persistence.IdempotencyRepository;
import com.paysentry.rules.RuleEngine;
import com.paysentry.rules.RuleResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScoreService {

    private final IdempotencyRepository idempotencyRepository;
    private final AuditLogRepository auditLogRepository;
    private final RuleEngine ruleEngine;
    private final ObjectMapper objectMapper;

    public ScoreService(
            IdempotencyRepository idempotencyRepository,
            AuditLogRepository auditLogRepository,
            RuleEngine ruleEngine,
            ObjectMapper objectMapper) {
        this.idempotencyRepository = idempotencyRepository;
        this.auditLogRepository = auditLogRepository;
        this.ruleEngine = ruleEngine;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ScoreResponse score(String idempotencyKey, ScoreRequest request) {
        return idempotencyRepository.findResponseJson(idempotencyKey)
                .map(this::readResponse)
                .orElseGet(() -> scoreNew(idempotencyKey, request));
    }

    private ScoreResponse scoreNew(String idempotencyKey, ScoreRequest request) {
        RuleResult result = ruleEngine.evaluate(request);
        ScoreResponse response = new ScoreResponse(
                result.decision(),
                result.riskScore(),
                result.reasonCodes());

        auditLogRepository.insert(
                writeJson(request),
                writeJson(result.reasonCodes()),
                result.decision().name(),
                result.riskScore());
        idempotencyRepository.insert(idempotencyKey, writeJson(response));
        return response;
    }

    private ScoreResponse readResponse(String json) {
        try {
            return objectMapper.readValue(json, ScoreResponse.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Stored idempotency response is invalid JSON", e);
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize JSON", e);
        }
    }
}
