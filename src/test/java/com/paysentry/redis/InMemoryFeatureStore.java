package com.paysentry.redis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory fake of Redis feature storage for unit tests.
 */
public class InMemoryFeatureStore implements FeatureStore {

    private final Map<String, Map<String, Double>> velocity = new ConcurrentHashMap<>();
    private final Map<String, double[]> amounts = new ConcurrentHashMap<>();
    private final Map<String, String> countries = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> devices = new ConcurrentHashMap<>();

    @Override
    public long recordAndCountVelocity(String cardId, long epochMillis, long windowMillis) {
        Map<String, Double> set = velocity.computeIfAbsent(cardId, ignored -> new HashMap<>());
        set.put(epochMillis + ":" + set.size(), (double) epochMillis);
        long minScore = epochMillis - windowMillis;
        set.entrySet().removeIf(entry -> entry.getValue() < minScore);
        return set.size();
    }

    @Override
    public OptionalDouble getRunningAverageAmount(String cardId) {
        double[] stats = amounts.get(cardId);
        if (stats == null || stats[1] <= 0) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(stats[0] / stats[1]);
    }

    @Override
    public void recordAmount(String cardId, double amount) {
        amounts.compute(cardId, (key, existing) -> {
            if (existing == null) {
                return new double[]{amount, 1};
            }
            existing[0] += amount;
            existing[1] += 1;
            return existing;
        });
    }

    @Override
    public Optional<String> getLastCountry(String cardId) {
        return Optional.ofNullable(countries.get(cardId));
    }

    @Override
    public void setLastCountry(String cardId, String country) {
        countries.put(cardId, country);
    }

    @Override
    public boolean isKnownDevice(String cardId, String deviceId) {
        Set<String> known = devices.get(cardId);
        return known != null && known.contains(deviceId);
    }

    @Override
    public void addDevice(String cardId, String deviceId) {
        devices.computeIfAbsent(cardId, ignored -> new HashSet<>()).add(deviceId);
    }
}
