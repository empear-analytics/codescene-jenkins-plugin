package org.jenkinsci.plugins.codescene;

import org.jenkinsci.plugins.codescene.Domain.Commit;
import org.jenkinsci.plugins.codescene.Domain.RiskClassification;
import org.jenkinsci.plugins.codescene.Domain.Warning;

import java.net.URL;
import java.util.List;

public class CodeSceneBuildActionEntry {
    private final List<Commit> commits;
    private final RiskClassification risk;
    private final List<Warning> warnings;
    private final URL viewUrl;

    public CodeSceneBuildActionEntry(List<Commit> commits, RiskClassification risk, List<Warning> warnings, URL viewUrl) {
        this.commits = commits;
        this.risk = risk;
        this.warnings = warnings;
        this.viewUrl = viewUrl;
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

    public String getTitle() {
        switch (commits.size()) {
            case 0: return "No Commits";
            case 1: return commits.get(0).value();
            default:
                StringBuilder builder = new StringBuilder();
                for (Commit c : commits) {
                    builder.append(c.value());
                    builder.append(", ");
                }
                return builder.toString();
        }
    }
}
