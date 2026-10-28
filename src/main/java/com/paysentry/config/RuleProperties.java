package com.paysentry.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "paysentry")
public class RuleProperties {

    private Rules rules = new Rules();
    private Thresholds thresholds = new Thresholds();

    public Rules getRules() {
        return rules;
    }

    public void setRules(Rules rules) {
        this.rules = rules;
    }

    public Thresholds getThresholds() {
        return thresholds;
    }

    public void setThresholds(Thresholds thresholds) {
        this.thresholds = thresholds;
    }

    public static class Rules {
        private Velocity1h velocity1h = new Velocity1h();
        private HighAmount highAmount = new HighAmount();
        private GeoMismatch geoMismatch = new GeoMismatch();
        private NewDevice newDevice = new NewDevice();

        public Velocity1h getVelocity1h() {
            return velocity1h;
        }

        public void setVelocity1h(Velocity1h velocity1h) {
            this.velocity1h = velocity1h;
        }

        public HighAmount getHighAmount() {
            return highAmount;
        }

        public void setHighAmount(HighAmount highAmount) {
            this.highAmount = highAmount;
        }

        public GeoMismatch getGeoMismatch() {
            return geoMismatch;
        }

        public void setGeoMismatch(GeoMismatch geoMismatch) {
            this.geoMismatch = geoMismatch;
        }

        public NewDevice getNewDevice() {
            return newDevice;
        }

        public void setNewDevice(NewDevice newDevice) {
            this.newDevice = newDevice;
        }
    }

    public static class Velocity1h {
        private double weight = 0.40;
        private int maxTransactions = 5;
        private long windowSeconds = 3600;

        public double getWeight() {
            return weight;
        }

        public void setWeight(double weight) {
            this.weight = weight;
        }

        public int getMaxTransactions() {
            return maxTransactions;
        }

        public void setMaxTransactions(int maxTransactions) {
            this.maxTransactions = maxTransactions;
        }

        public long getWindowSeconds() {
            return windowSeconds;
        }

        public void setWindowSeconds(long windowSeconds) {
            this.windowSeconds = windowSeconds;
        }
    }

    public static class HighAmount {
        private double weight = 0.35;
        private double multiplier = 3.0;

        public double getWeight() {
            return weight;
        }

        public void setWeight(double weight) {
            this.weight = weight;
        }

        public double getMultiplier() {
            return multiplier;
        }

        public void setMultiplier(double multiplier) {
            this.multiplier = multiplier;
        }
    }

    public static class GeoMismatch {
        private double weight = 0.30;

        public double getWeight() {
            return weight;
        }

        public void setWeight(double weight) {
            this.weight = weight;
        }
    }

    public static class NewDevice {
        private double weight = 0.15;

        public double getWeight() {
            return weight;
        }

        public void setWeight(double weight) {
            this.weight = weight;
        }
    }

    public static class Thresholds {
        private double approveBelow = 0.3;
        private double reviewBelow = 0.7;

        public double getApproveBelow() {
            return approveBelow;
        }

        public void setApproveBelow(double approveBelow) {
            this.approveBelow = approveBelow;
        }

        public double getReviewBelow() {
            return reviewBelow;
        }

        public void setReviewBelow(double reviewBelow) {
            this.reviewBelow = reviewBelow;
        }
    }
}
