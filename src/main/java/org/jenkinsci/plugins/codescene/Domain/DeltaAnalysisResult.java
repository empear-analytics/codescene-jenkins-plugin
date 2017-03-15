package org.jenkinsci.plugins.codescene.Domain;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.util.ArrayList;
import java.util.List;

public class DeltaAnalysisResult {
    private final RiskClassification risk;
    private final Warnings warnings;

    public DeltaAnalysisResult(final JsonObject result) {
        ensureTheVersionIsSupported(result);

        final JsonObject deltaResult = result.getJsonObject("result");

        risk = riskFrom(deltaResult);
        warnings = warningsFrom(deltaResult);
    }

    private RiskClassification riskFrom(JsonObject deltaResult) {
        return new RiskClassification(deltaResult.getJsonNumber("risk").intValue());
    }

    private static Warnings warningsFrom(JsonObject deltaResult) {
        final Warnings ws = new Warnings();

        final JsonArray jsonWarnings = deltaResult.getJsonArray("warnings");

        for (int i = 0; i < jsonWarnings.size(); ++i) {
            final JsonObject w = jsonWarnings.getJsonObject(i);

            final WarningCategory category = new WarningCategory(w.getString("category"));
            final JsonArray jsonDetails = w.getJsonArray("details");

            List<String> ds = new ArrayList<>();
            for (int j=0; j < jsonDetails.size(); j++) {
                ds.add(jsonDetails.getString(j));
            }

            final WarningDetails details = WarningDetails.from(ds);

            ws.add(new Warning(category, details));
        }

        return ws;
    }

    private void ensureTheVersionIsSupported(JsonObject result) {
        final String version = result.getString("version");

        if (!version.equals("1")) {
            throw new RuntimeException("The CodeScene API reports version " + version + ", which we don't know. You need to upgrade this plug-in.");
        }
    }

    public RiskClassification risk() {
        return risk;
    }

    public Warnings warnings() {
        return warnings;
    }
}
