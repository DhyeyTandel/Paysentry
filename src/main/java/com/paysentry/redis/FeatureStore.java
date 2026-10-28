package com.paysentry.redis;

import java.util.Optional;
import java.util.OptionalDouble;

/**
 * Redis-backed card feature access. Implementations use sorted sets for velocity,
 * hashes for running amount averages, strings for last country, and sets for devices.
 */
public interface FeatureStore {

    /**
     * Records a transaction timestamp for the card (ZADD), drops entries older than
     * the window (ZREMRANGEBYSCORE), and returns the count in the window (ZCARD).
     */
    long recordAndCountVelocity(String cardId, long epochMillis, long windowMillis);

    OptionalDouble getRunningAverageAmount(String cardId);

    void recordAmount(String cardId, double amount);

    Optional<String> getLastCountry(String cardId);

    void setLastCountry(String cardId, String country);

    boolean isKnownDevice(String cardId, String deviceId);

    void addDevice(String cardId, String deviceId);
}
