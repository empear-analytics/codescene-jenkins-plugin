package org.jenkinsci.plugins.codescene.Domain;

import java.net.URL;

public class Configuration {

    private final URL url;
    private final CodeSceneUser user;
    private final Repository repo;

    public Configuration(final URL codeSceneUrl, final CodeSceneUser user, final Repository gitRepositoryToAnalyze) {
        url = codeSceneUrl;
        this.user = user;
        repo = gitRepositoryToAnalyze;
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
}
