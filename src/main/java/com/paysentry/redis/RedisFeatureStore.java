package com.paysentry.redis;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.UUID;

@Component
public class RedisFeatureStore implements FeatureStore {

    private final StringRedisTemplate redis;

    public RedisFeatureStore(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public long recordAndCountVelocity(String cardId, long epochMillis, long windowMillis) {
        String key = "vel:" + cardId;
        String member = epochMillis + ":" + UUID.randomUUID();
        redis.opsForZSet().add(key, member, epochMillis);
        long minScore = epochMillis - windowMillis;
        redis.opsForZSet().removeRangeByScore(key, Double.NEGATIVE_INFINITY, minScore - 1);
        Long count = redis.opsForZSet().zCard(key);
        return count == null ? 0L : count;
    }

    @Override
    public OptionalDouble getRunningAverageAmount(String cardId) {
        Map<Object, Object> entries = redis.opsForHash().entries("amt:" + cardId);
        if (entries.isEmpty()) {
            return OptionalDouble.empty();
        }
        double sum = Double.parseDouble(String.valueOf(entries.get("sum")));
        long count = Long.parseLong(String.valueOf(entries.get("count")));
        if (count <= 0) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(sum / count);
    }

    @Override
    public void recordAmount(String cardId, double amount) {
        String key = "amt:" + cardId;
        redis.opsForHash().increment(key, "sum", amount);
        redis.opsForHash().increment(key, "count", 1);
    }

    @Override
    public Optional<String> getLastCountry(String cardId) {
        return Optional.ofNullable(redis.opsForValue().get("geo:" + cardId));
    }

    @Override
    public void setLastCountry(String cardId, String country) {
        redis.opsForValue().set("geo:" + cardId, country);
    }

    @Override
    public boolean isKnownDevice(String cardId, String deviceId) {
        Boolean member = redis.opsForSet().isMember("dev:" + cardId, deviceId);
        return Boolean.TRUE.equals(member);
    }

    @Override
    public void addDevice(String cardId, String deviceId) {
        redis.opsForSet().add("dev:" + cardId, deviceId);
    }
}
