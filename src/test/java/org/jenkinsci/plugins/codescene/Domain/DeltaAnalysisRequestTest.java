package org.jenkinsci.plugins.codescene.Domain;

import org.junit.Test;

import static org.junit.Assert.*;

public class DeltaAnalysisRequestTest {

    @Test
    public void serializesRequestAsJson() {
        final Commits commits = Commits.from(new Commit("b75943ac51bf48ff5a206f0854ace2b67734ea66"));
        final Repository gitRepo = new Repository("codescene-ui");

        final DeltaAnalysisRequest request = new DeltaAnalysisRequest(commits, gitRepo);

        assertEquals("{\"commits\":[\"b75943ac51bf48ff5a206f0854ace2b67734ea66\"],\"repository\":\"codescene-ui\"}", request.asJson().toString());
    }

    @Test
    public void serializesRequestWithMultipleCommitsAsJson() {
        final Commits commits = Commits.from(new Commit("b75943ac5"), new Commit("9822ac"));
        final Repository gitRepo = new Repository("codescene-ui");

        final DeltaAnalysisRequest request = new DeltaAnalysisRequest(commits, gitRepo);

        assertEquals("{\"commits\":[\"b75943ac5\",\"9822ac\"],\"repository\":\"codescene-ui\"}", request.asJson().toString());
    }
}