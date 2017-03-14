package org.jenkinsci.plugins.codescene;

import org.apache.http.client.methods.HttpPost;
import org.jenkinsci.plugins.codescene.Domain.*;

import java.net.URISyntaxException;

public class DeltaAnalysis {

    private final Configuration config;

    public DeltaAnalysis(Configuration config) {
        this.config = config;
    }

    public DeltaAnalysisResult runOn(final Commits commits) {
        final DeltaAnalysisRequest payload = new DeltaAnalysisRequest(commits, config.gitRepisitoryToAnalyze());

        try {
            return synchronousRequestWith(payload);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("The configured CodeScene URL isn't valid", e);
        }
    }


    private DeltaAnalysisResult synchronousRequestWith(final DeltaAnalysisRequest payload) throws URISyntaxException {
        HttpPost post = new HttpPost(config.codeSceneUrl().toURI());

        final CodeSceneUser user = config.user();

        // TODO: 1. add Basic Authorization to the header based on the user info - we need to base64 encoded it.
        //       2. add the payload as body to the POST request.
        //       3. De-serialize the response into a domain type (DeltaAnalysisResult).


        return new DeltaAnalysisResult();
    }
}
