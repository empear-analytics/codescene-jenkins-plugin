package org.jenkinsci.plugins.codescene.Domain;

import java.util.ArrayList;
import java.util.List;

public class Warnings {
    private final List<Warning> ws = new ArrayList<>();

    public void add(final Warning w) {
        ws.add(w);
    }

    public List<Warning> value() {
        return new ArrayList<>(ws);
    }
}
