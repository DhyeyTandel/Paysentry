package com.paysentry.api;

import java.util.List;

public record ScoreResponse(
        Decision decision,
        double riskScore,
        List<String> reasonCodes
) {
}
