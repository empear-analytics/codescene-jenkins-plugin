package org.jenkinsci.plugins.codescene;

import hudson.model.Action;
import org.kohsuke.stapler.StaplerProxy;

import java.util.ArrayList;

public class CodeSceneBuildAction implements Action, StaplerProxy {
    private final ArrayList<CodeSceneBuildActionEntry> entries;

    public CodeSceneBuildAction(ArrayList<CodeSceneBuildActionEntry> entries) {
        this.entries = entries;
    }

    public ArrayList<CodeSceneBuildActionEntry> getEntries() {
        return entries;
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return null;
    }

    @Override
    public Object getTarget() {
        return null;
    }
}
