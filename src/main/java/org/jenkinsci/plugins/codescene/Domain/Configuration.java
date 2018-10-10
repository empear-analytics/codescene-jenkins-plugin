package org.jenkinsci.plugins.codescene.Domain;

import java.net.URL;

public class Configuration {

    private final URL url;
    private final CodeSceneUser user;
    private final Repository repo;
    private final int couplingThresholdPercent;
    private final boolean useBiomarkers;
    private final boolean failBuildOnFailedAnalysis;

    public Configuration(final URL codeSceneUrl, final CodeSceneUser user, final Repository gitRepositoryToAnalyze,
                         int couplingThresholdPercent, boolean useBiomarkers,
                         boolean failBuildOnFailedAnalysis) {
        this.url = codeSceneUrl;
        this.user = user;
        this.repo = gitRepositoryToAnalyze;
        this.couplingThresholdPercent = couplingThresholdPercent;
        this.useBiomarkers = useBiomarkers;
        this.failBuildOnFailedAnalysis = failBuildOnFailedAnalysis;
    }

    public URL codeSceneUrl() {
        return url;
    }

    public CodeSceneUser user() {
        return user;
    }

    public Repository gitRepisitoryToAnalyze() {
        return repo;
    }

    public int couplingThresholdPercent() {
        return couplingThresholdPercent;
    }

    public boolean useBiomarkers() {
        return useBiomarkers;
    }

    public boolean failBuildOnFailedAnalysis() { return failBuildOnFailedAnalysis; }
}
