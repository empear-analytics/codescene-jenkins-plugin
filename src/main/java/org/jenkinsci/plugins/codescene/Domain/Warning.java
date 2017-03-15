package org.jenkinsci.plugins.codescene.Domain;

import java.util.List;

public class Warning {
    final private WarningCategory category;
    final private WarningDetails details;

    public Warning(WarningCategory category, WarningDetails details) {
        this.category = category;
        this.details = details;
    }

    public WarningCategory category() {
        return category;
    }

    public WarningDetails details() {
        return details;
    }
}
