package org.jenkinsci.plugins.codescene.Domain;

import javax.json.*;


public class DeltaAnalysisRequest {

    private final JsonObject value;

    public DeltaAnalysisRequest(final Commits commits, final Repository gitRepository, final int couplingThresholdPercent,
                                boolean useBiomarkers) {
        final JsonArray cs = serialize(commits);

        JsonObjectBuilder b = Json.createObjectBuilder();
        b.add("commits", cs);
        b.add("repository", gitRepository.value());
        b.add("coupling_threshold_percent", couplingThresholdPercent);
        b.add("use_biomarkers", useBiomarkers);

        value = b.build();
    }

    private static JsonArray serialize(final Commits commits) {
        final JsonArrayBuilder b = Json.createArrayBuilder();

        for (Commit c : commits.value()) {
            b.add(c.value());
        }

        return b.build();
    }

    public JsonObject asJson() {
        return value;
    }
}
