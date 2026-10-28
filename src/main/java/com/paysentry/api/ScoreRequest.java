package com.paysentry.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;

public record ScoreRequest(
        @NotBlank String cardId,
        @NotBlank String deviceId,
        @NotBlank String merchantId,
        @NotNull @Positive Double amount,
        @NotBlank String country,
        @NotNull Instant timestamp
) {
}
