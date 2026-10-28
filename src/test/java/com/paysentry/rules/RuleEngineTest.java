package com.paysentry.rules;

import com.paysentry.api.Decision;
import com.paysentry.api.ScoreRequest;
import com.paysentry.config.RuleProperties;
import com.paysentry.redis.InMemoryFeatureStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuleEngineTest {

    private InMemoryFeatureStore featureStore;
    private RuleProperties properties;
    private RuleEngine engine;

    @BeforeEach
    void setUp() {
        featureStore = new InMemoryFeatureStore();
        properties = new RuleProperties();
        engine = new RuleEngine(featureStore, properties);
    }

    @Test
    void velocity1hFiresWhenMoreThanFiveTransactionsInOneHour() {
        Instant base = Instant.parse("2024-01-01T12:00:00Z");
        for (int i = 0; i < 5; i++) {
            engine.evaluate(request("card-1", "dev-a", 10.0, "US", base.plusSeconds(i)));
        }

        RuleResult result = engine.evaluate(request("card-1", "dev-a", 10.0, "US", base.plusSeconds(10)));

        assertTrue(result.reasonCodes().contains(RuleEngine.VELOCITY_1H));
    }

    @Test
    void highAmountFiresWhenAmountExceedsThreeTimesRunningAverage() {
        Instant t0 = Instant.parse("2024-01-01T12:00:00Z");
        engine.evaluate(request("card-2", "dev-a", 100.0, "US", t0));

        RuleResult result = engine.evaluate(
                request("card-2", "dev-a", 301.0, "US", t0.plusSeconds(1)));

        assertTrue(result.reasonCodes().contains(RuleEngine.HIGH_AMOUNT));
    }

    @Test
    void geoMismatchFiresWhenCountryDiffersFromLast() {
        Instant t0 = Instant.parse("2024-01-01T12:00:00Z");
        engine.evaluate(request("card-3", "dev-a", 50.0, "US", t0));

        RuleResult result = engine.evaluate(
                request("card-3", "dev-a", 50.0, "FR", t0.plusSeconds(1)));

        assertTrue(result.reasonCodes().contains(RuleEngine.GEO_MISMATCH));
    }

    @Test
    void newDeviceFiresWhenDeviceNotSeenForCard() {
        Instant t0 = Instant.parse("2024-01-01T12:00:00Z");
        engine.evaluate(request("card-4", "dev-a", 50.0, "US", t0));

        RuleResult firstSeen = engine.evaluate(
                request("card-4", "dev-a", 50.0, "US", t0.plusSeconds(1)));
        RuleResult newDevice = engine.evaluate(
                request("card-4", "dev-b", 50.0, "US", t0.plusSeconds(2)));

        assertFalse(firstSeen.reasonCodes().contains(RuleEngine.NEW_DEVICE));
        assertTrue(newDevice.reasonCodes().contains(RuleEngine.NEW_DEVICE));
    }

    @Test
    void decisionIsApproveBelowPointThree() {
        assertEquals(Decision.APPROVE, engine.decide(0.0));
        assertEquals(Decision.APPROVE, engine.decide(0.29));
    }

    @Test
    void decisionIsReviewBelowPointSeven() {
        assertEquals(Decision.REVIEW, engine.decide(0.3));
        assertEquals(Decision.REVIEW, engine.decide(0.69));
    }

    @Test
    void decisionIsDeclineAtOrAbovePointSeven() {
        assertEquals(Decision.DECLINE, engine.decide(0.7));
        assertEquals(Decision.DECLINE, engine.decide(1.0));
    }

    private static ScoreRequest request(
            String cardId, String deviceId, double amount, String country, Instant timestamp) {
        return new ScoreRequest(cardId, deviceId, "merchant-1", amount, country, timestamp);
    }
}
