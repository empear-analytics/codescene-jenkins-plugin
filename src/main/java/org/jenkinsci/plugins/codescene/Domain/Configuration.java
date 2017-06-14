package org.jenkinsci.plugins.codescene.Domain;

import java.net.URL;

public class Configuration {

    private final URL url;
    private final CodeSceneUser user;
    private final Repository repo;
    private final int couplingThresholdPercent;

    public Configuration(final URL codeSceneUrl, final CodeSceneUser user, final Repository gitRepositoryToAnalyze,
                         int couplingThresholdPercent) {
        this.url = codeSceneUrl;
        this.user = user;
        this.repo = gitRepositoryToAnalyze;
        this.couplingThresholdPercent = couplingThresholdPercent;
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
}
