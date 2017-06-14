package org.jenkinsci.plugins.codescene.Domain;

import org.junit.Test;

import static org.junit.Assert.*;

public class DeltaAnalysisRequestTest {

    private static final Repository GIT_REPO = new Repository("codescene-ui");
    private static final int COUPLING_THRESHOLD = 65;

    @Test
    public void serializesRequestAsJson() {
        final DeltaAnalysisRequest request = new DeltaAnalysisRequest(
                Commits.from(new Commit("b75943ac51bf48ff5a206f0854ace2b67734ea66")), GIT_REPO, COUPLING_THRESHOLD);

        assertEquals("{\"commits\":[\"b75943ac51bf48ff5a206f0854ace2b67734ea66\"]," +
                        "\"repository\":\"codescene-ui\"," +
                        "\"coupling_threshold_percent\":65}",
                request.asJson().toString());
    }

    @Test
    public void serializesRequestWithMultipleCommitsAsJson() {
        final DeltaAnalysisRequest request = new DeltaAnalysisRequest(
                Commits.from(new Commit("b75943ac5"), new Commit("9822ac")), GIT_REPO, COUPLING_THRESHOLD);

        assertEquals("{\"commits\":[\"b75943ac5\",\"9822ac\"]," +
                        "\"repository\":\"codescene-ui\"," +
                        "\"coupling_threshold_percent\":65}",
                request.asJson().toString());
    }
}