package org.jenkinsci.plugins.codescene;

import hudson.EnvVars;
import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.codescene.Domain.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class CodeSceneBuilder extends Builder implements SimpleBuildStep {

    private final boolean analyzeLatestIndividually;
    private final String baseRevision;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public CodeSceneBuilder(boolean analyzeLatestIndividually, String baseRevision) {
        this.analyzeLatestIndividually = analyzeLatestIndividually;
        this.baseRevision = baseRevision;
    }

    public boolean getAnalyzeLatestIndividually() {
        return analyzeLatestIndividually;
    }

    public String getBaseRevision() {
        return baseRevision;
    }

    private ArrayList<Commits> parseCommits(String output) {
        ArrayList<Commits> commitSets = new ArrayList<>();
        for (String line : output.split("\n")) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                commitSets.add(Commits.from(new Commit(trimmed)));
            }
        }
        return commitSets;
    }

    private ArrayList<CodeSceneBuildActionEntry> runDeltaAnalyses(Configuration config, String output, TaskListener listener) throws MalformedURLException {
        ArrayList<Commits> commitSets = parseCommits(output);
        ArrayList<CodeSceneBuildActionEntry> entries = new ArrayList<>(commitSets.size());

        if (!commitSets.isEmpty()) {
            listener.getLogger().format("Starting delta analysis on %d commit(s)...\n", commitSets.size());
            for (Commits commits : commitSets) {
                DeltaAnalysis deltaAnalysis = new DeltaAnalysis(config);
                listener.getLogger().format("Running delta analysis on commits (%s) in repository %s.\n", commits.value(), config.gitRepisitoryToAnalyze().value());
                DeltaAnalysisResult result = deltaAnalysis.runOn(commits);

                URL detailsUrl = new URL(
                        config.codeSceneUrl().getProtocol(),
                        config.codeSceneUrl().getHost(),
                        config.codeSceneUrl().getPort(),
                        result.getViewUrl());
                entries.add(new CodeSceneBuildActionEntry(commits.value(), result.getRisk(), result.getWarnings().value(), detailsUrl));
            }
        } else {
            listener.getLogger().format("No commits to run delta analysis on.\n");
        }

        return entries;
    }

    @Override
    public void perform(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener) {
        try {
            if (!getAnalyzeLatestIndividually()) {
                listener.getLogger().println("Not analyzing individual commits.");
                return;
            }

            URL codesceneUrl = new URL("http", "localhost", 3003, "/projects/13/delta-analysis");
            CodeSceneUser codeSceneUser = new CodeSceneUser("Foo", "Foo");
            Repository codeSceneGitRepository = new Repository("empear-enterprise");
            Configuration codesceneConfig = new Configuration(codesceneUrl, codeSceneUser, codeSceneGitRepository);
            EnvVars env = build.getEnvironment(listener);

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            launcher.launch()
                    .cmdAsSingleString(String.format("git log --pretty='%%H' %s..%s", getBaseRevision(), env.get("GIT_COMMIT")))
                    .pwd(workspace)
                    .envs(build.getEnvironment(listener))
                    .stdout(out)
                    .join();

            ArrayList<CodeSceneBuildActionEntry> entries = runDeltaAnalyses(codesceneConfig, out.toString(), listener);
            build.addAction(new CodeSceneBuildAction(entries));

        } catch (InterruptedException e) {
            listener.error("Failed to run delta analysis", e);
        } catch (IOException e) {
            listener.error("Failed to run delta analysis", e);
        }
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        public DescriptorImpl() {
            load();
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        @Override
        public String getDisplayName() {
            return "Run CodeScene Delta Analysis";
        }

        /**
         * Applicable to any kind of project.
         */
        @Override
        public boolean isApplicable(Class type) {
            return true;
        }

        @Override
        public boolean configure(StaplerRequest staplerRequest, JSONObject json) throws FormException {
            save();
            return true; // indicate that everything is good so far
        }
    }
}

