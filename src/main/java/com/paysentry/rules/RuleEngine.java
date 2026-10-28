package com.paysentry.rules;

import com.paysentry.api.Decision;
import com.paysentry.api.ScoreRequest;
import com.paysentry.config.RuleProperties;
import com.paysentry.redis.FeatureStore;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

@Component
public class RuleEngine {

    public static final String VELOCITY_1H = "VELOCITY_1H";
    public static final String HIGH_AMOUNT = "HIGH_AMOUNT";
    public static final String GEO_MISMATCH = "GEO_MISMATCH";
    public static final String NEW_DEVICE = "NEW_DEVICE";

    private final FeatureStore featureStore;
    private final RuleProperties properties;

    public RuleEngine(FeatureStore featureStore, RuleProperties properties) {
        this.featureStore = featureStore;
        this.properties = properties;
    }

    public RuleResult evaluate(ScoreRequest request) {
        List<String> fired = new ArrayList<>();
        double score = 0.0;

        RuleProperties.Rules rules = properties.getRules();
        long epochMillis = request.timestamp().toEpochMilli();
        long windowMillis = rules.getVelocity1h().getWindowSeconds() * 1000L;

        long velocityCount = featureStore.recordAndCountVelocity(
                request.cardId(), epochMillis, windowMillis);
        if (velocityCount > rules.getVelocity1h().getMaxTransactions()) {
            fired.add(VELOCITY_1H);
            score += rules.getVelocity1h().getWeight();
        }

        OptionalDouble average = featureStore.getRunningAverageAmount(request.cardId());
        if (average.isPresent()
                && request.amount() > average.getAsDouble() * rules.getHighAmount().getMultiplier()) {
            fired.add(HIGH_AMOUNT);
            score += rules.getHighAmount().getWeight();
        }
        featureStore.recordAmount(request.cardId(), request.amount());

        Optional<String> lastCountry = featureStore.getLastCountry(request.cardId());
        if (lastCountry.isPresent() && !lastCountry.get().equals(request.country())) {
            fired.add(GEO_MISMATCH);
            score += rules.getGeoMismatch().getWeight();
        }
        featureStore.setLastCountry(request.cardId(), request.country());

        if (!featureStore.isKnownDevice(request.cardId(), request.deviceId())) {
            fired.add(NEW_DEVICE);
            score += rules.getNewDevice().getWeight();
        }
        featureStore.addDevice(request.cardId(), request.deviceId());

        score = Math.min(1.0, score);
        Decision decision = decide(score);
        return new RuleResult(decision, score, List.copyOf(fired));
    }

    Decision decide(double score) {
        RuleProperties.Thresholds thresholds = properties.getThresholds();
        if (score < thresholds.getApproveBelow()) {
            return Decision.APPROVE;
        }
        if (score < thresholds.getReviewBelow()) {
            return Decision.REVIEW;
        }
        return Decision.DECLINE;
    }
}
