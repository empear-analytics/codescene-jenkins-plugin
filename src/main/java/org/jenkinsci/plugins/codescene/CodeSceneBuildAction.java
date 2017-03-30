package org.jenkinsci.plugins.codescene;

import hudson.model.Action;
import org.kohsuke.stapler.StaplerProxy;

import java.util.List;

public class CodeSceneBuildAction implements Action, StaplerProxy {
    private final String title;
    private final List<CodeSceneBuildActionEntry> entries;

    public CodeSceneBuildAction(String title, List<CodeSceneBuildActionEntry> entries) {
        this.title = title;
        this.entries = entries;
    }

    public String getTitle() {
        return title;
    }

    public List<CodeSceneBuildActionEntry> getEntries() {
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
