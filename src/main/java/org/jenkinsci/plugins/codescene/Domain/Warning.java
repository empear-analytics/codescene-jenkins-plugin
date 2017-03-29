package org.jenkinsci.plugins.codescene.Domain;

import java.util.List;

public class Warning {
    final private WarningCategory category;
    final private List<String> details;

    public Warning(WarningCategory category, List<String> details) {
        this.category = category;
        this.details = details;
    }

    public WarningCategory getCategory() {
        return category;
    }

    public List<String> getDetails() {
        return details;
    }
}
