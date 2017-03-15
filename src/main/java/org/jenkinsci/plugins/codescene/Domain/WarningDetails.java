package org.jenkinsci.plugins.codescene.Domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WarningDetails {
    private final List<String> ws;


    public static WarningDetails from(List<String> a) {
        if (a.size() < 1) {
            throw new IllegalArgumentException("You need to provide at least one warning detail");
        }

        return new WarningDetails(a);
    }

    private WarningDetails(List<String> cs) {
        ws = cs;
    }

    public List<String> value() {
        return new ArrayList<>(ws);
    }
}
