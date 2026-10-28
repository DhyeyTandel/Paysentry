package com.paysentry.rules;

import com.paysentry.api.Decision;

import java.util.List;

public record RuleResult(
        Decision decision,
        double riskScore,
        List<String> reasonCodes
) {
}
