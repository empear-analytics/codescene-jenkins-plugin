package org.jenkinsci.plugins.codescene;

import org.apache.http.*;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.jenkinsci.plugins.codescene.Domain.*;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.IOException;
import java.net.URISyntaxException;

public class DeltaAnalysis {

    private final Configuration config;

    public DeltaAnalysis(Configuration config) {
        this.config = config;
    }

    public DeltaAnalysisResult runOn(final Commits commits) throws RemoteAnalysisException {
        final DeltaAnalysisRequest payload = new DeltaAnalysisRequest(commits, config.gitRepisitoryToAnalyze(),
                config.couplingThresholdPercent(), config.useBiomarkers());

        try {
            return synchronousRequestWith(payload, commits);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("The configured CodeScene URL isn't valid", e);
        } catch (IOException e) {
            throw new RemoteAnalysisException("Failed to send request to CodeScene at " + config.codeSceneUrl().toString(), e);
        }
    }


    private DeltaAnalysisResult synchronousRequestWith(final DeltaAnalysisRequest payload, Commits commits) throws RemoteAnalysisException, URISyntaxException, IOException {
        final HttpPost codeSceneRequest = createRequestFor(payload);
        final CloseableHttpClient httpclient = HttpClients.createDefault();

        try {
            final HttpResponse rawResponse = httpclient.execute(codeSceneRequest);
            final StatusLine status = rawResponse.getStatusLine();

            if (HttpStatus.SC_CREATED == status.getStatusCode()) {
                return parseSuccessfulAnalysisResults(rawResponse, commits);
            }

            reportFailureAsException(rawResponse);

        } finally {
            httpclient.close();
        }

        throw new RuntimeException("Internal error: we failed to deal properly with the request.");
    }

    private void reportFailureAsException(HttpResponse rawResponse) throws IOException, RemoteAnalysisException {
        final HttpEntity responseBody = rawResponse.getEntity();
        final String errorMessage = EntityUtils.toString(responseBody);

        throw new RemoteAnalysisException(String.format("Failed to execute delta analysis. Status: %s, Reason: %s", rawResponse.getStatusLine(), errorMessage));
    }

    private DeltaAnalysisResult parseSuccessfulAnalysisResults(HttpResponse rawResponse, Commits commits) throws IOException, RemoteAnalysisException {
        final HttpEntity responseBody = rawResponse.getEntity();

        if (responseBody == null) {
            throw new RemoteAnalysisException("Internal error: The delta analysis was a success but failed to parse the returned results");
        }

        final JsonReader reader = Json.createReader(responseBody.getContent());
        final JsonObject delta = reader.readObject();

        return new DeltaAnalysisResult(commits, delta);
    }

    private HttpPost createRequestFor(final DeltaAnalysisRequest payload) throws URISyntaxException {
        final CodeSceneUser user = config.user();

        final Header authorization = new BasicHeader("Authorization", "Basic " + user.asBase64Encoded());

        HttpPost codeSceneRequest = new HttpPost(config.codeSceneUrl().toURI());
        codeSceneRequest.addHeader(authorization);

        StringEntity requestEntity = new StringEntity(
                payload.asJson().toString(),
                ContentType.APPLICATION_JSON);
        codeSceneRequest.setEntity(requestEntity);

        return codeSceneRequest;
    }
}
