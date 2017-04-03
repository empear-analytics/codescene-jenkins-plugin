package org.jenkinsci.plugins.codescene;

import org.jenkinsci.plugins.codescene.Domain.Commit;
import org.jenkinsci.plugins.codescene.Domain.RiskClassification;
import org.jenkinsci.plugins.codescene.Domain.Warning;

import java.net.URL;
import java.util.List;

public class CodeSceneBuildActionEntry {
    private final String title;
    private final boolean showCommits;
    private final List<Commit> commits;
    private final RiskClassification risk;
    private final List<Warning> warnings;
    private final URL viewUrl;
    private final int riskThreshold;

    public CodeSceneBuildActionEntry(String title, boolean showCommits, List<Commit> commits, RiskClassification risk, List<Warning> warnings, URL viewUrl, int riskThreshold) {
        this.title = title;
        this.showCommits = showCommits;
        this.commits = commits;
        this.risk = risk;
        this.warnings = warnings;
        this.viewUrl = viewUrl;
        this.riskThreshold = riskThreshold;
    }

    public String getTitle() {
        return title;
    }

    public boolean getShowCommits() {
        return showCommits;
    }

    public List<Commit> getCommits() {
        return commits;
    }

    public RiskClassification getRisk() {
        return risk;
    }

    public List<Warning> getWarnings() {
        return warnings;
    }

    public boolean getHasWarnings() {
        return !warnings.isEmpty();
    }

    public URL getViewUrl() {
        return viewUrl;
    }

    public int getRiskThreshold() {
        return riskThreshold;
    }

    public boolean getHitsRiskThreshold() {
        return risk.getValue() >= riskThreshold;
    }
}
