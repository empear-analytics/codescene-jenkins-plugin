package org.jenkinsci.plugins.codescene.Domain;

public class WarningCategory {
    final private String category;

    public WarningCategory(String category) {
        if (category == null || category.isEmpty()) {
            throw new IllegalArgumentException("The getCategory has to be a valid string describing a specific type of warning from CodeScene.");
        }

        this.category = category;
    }

    public String value() {
        return category;
    }

    @Override
    public String toString() {
        return category;
    }
}
