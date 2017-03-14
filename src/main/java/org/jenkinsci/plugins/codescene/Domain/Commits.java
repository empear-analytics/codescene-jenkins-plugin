package org.jenkinsci.plugins.codescene.Domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Commits {

    private final List<Commit> vs;


    public static Commits from(Commit... a) {
        if (a.length < 1) {
            throw new IllegalArgumentException("You need to provide at least one commit");
        }

        return new Commits(Arrays.asList(a));
    }

    private Commits(List<Commit> cs) {
        vs = cs;
    }

    public List<Commit> value() {
        return new ArrayList<>(vs);
    }
}
