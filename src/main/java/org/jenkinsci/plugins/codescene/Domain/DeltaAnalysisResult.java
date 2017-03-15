package org.jenkinsci.plugins.codescene.Domain;

import javax.json.JsonObject;

public class DeltaAnalysisResult {
    private final RiskClassification risk;

    public DeltaAnalysisResult(final JsonObject result) {
        final String version = result.getString("version");

        if (!version.equals("1")) {
            throw new RuntimeException("The CodeScene API reports version " + version + ", which we don't know. You need to upgrade this plug-in.");
        }

        final JsonObject deltaResult = result.getJsonObject("result");

        risk = new RiskClassification(deltaResult.getJsonNumber("risk").intValue());
    }

    public RiskClassification risk() {
        return risk;
    }
}
