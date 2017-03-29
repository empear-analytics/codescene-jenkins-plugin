package org.jenkinsci.plugins.codescene.Domain;

public class RiskClassification {

    private final int risk;

    public RiskClassification(int risk) {

        if (risk > 10 || risk < 0) {
            throw new IllegalArgumentException("Risk has to be an ordinal between 1 and 10. Your getRisk value of " + risk + " doesn't match that constraint.");
        }

        this.risk = risk;
    }

    public int getValue() {
        return risk;
    }
}
