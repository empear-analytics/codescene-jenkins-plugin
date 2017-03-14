package org.jenkinsci.plugins.codescene.Domain;

public class Repository {

    private final String name;

    public Repository(final String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("A repository name must be a valid string");
        }
        this.name = name;
    }

    public String value() {
        return name;
    }
}
