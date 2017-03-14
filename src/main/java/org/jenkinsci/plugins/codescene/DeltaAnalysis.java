package org.jenkinsci.plugins.codescene;

import org.jenkinsci.plugins.codescene.Domain.Commits;
import org.jenkinsci.plugins.codescene.Domain.Configuration;
import org.jenkinsci.plugins.codescene.Domain.DeltaAnalysisResult;

public class DeltaAnalysis {

    private final Configuration config;

    public DeltaAnalysis(Configuration config) {
        this.config = config;
    }

    public DeltaAnalysisResult runOn(final Commits commits) {
        return new DeltaAnalysisResult();
    }
}
