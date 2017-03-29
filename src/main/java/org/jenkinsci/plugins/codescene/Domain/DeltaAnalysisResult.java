package org.jenkinsci.plugins.codescene.Domain;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;

public class DeltaAnalysisResult {
    private final String viewUrl;
    private final Commits commits;
    private final RiskClassification risk;
    private final Warnings warnings;

    public DeltaAnalysisResult(final Commits commits, final JsonObject result) {
        ensureTheVersionIsSupported(result);

        final JsonObject deltaResult = result.getJsonObject("result");

        viewUrl = result.getString("view");
        risk = riskFrom(deltaResult);
        warnings = warningsFrom(deltaResult);
        this.commits = commits;
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

            final List<String> details = new ArrayList<>(ds);

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

    public String getViewUrl() {
        return viewUrl;
    }

    public Commits getCommits() {
        return commits;
    }

    public RiskClassification getRisk() {
        return risk;
    }

    public Warnings getWarnings() {
        return warnings;
    }
}
