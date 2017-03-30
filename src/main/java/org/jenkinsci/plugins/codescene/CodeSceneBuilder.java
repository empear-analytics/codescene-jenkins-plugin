package org.jenkinsci.plugins.codescene;

import hudson.EnvVars;
import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import hudson.util.FormValidation;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.codescene.Domain.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CodeSceneBuilder extends Builder implements SimpleBuildStep {

    private final boolean analyzeLatestIndividually;
    private final boolean analyzeBranchDiff;
    private final String baseRevision;

    @DataBoundConstructor
    public CodeSceneBuilder(boolean analyzeLatestIndividually, boolean analyzeBranchDiff, String baseRevision) {
        this.analyzeLatestIndividually = analyzeLatestIndividually;
        this.analyzeBranchDiff = analyzeBranchDiff;
        this.baseRevision = baseRevision;
    }

    public boolean getAnalyzeLatestIndividually() {
        return analyzeLatestIndividually;
    }

    public boolean getAnalyzeBranchDiff() {
        return analyzeBranchDiff;
    }
    public String getBaseRevision() {
        return baseRevision;
    }

    private Commits revisionsAsCommitSet(List<String> revisions) {
        ArrayList<Commit> commits = new ArrayList<>();
        for (String revision : revisions) {
            commits.add(new Commit(revision));
        }
        return new Commits(commits);
    }

    private ArrayList<Commits> revisionsAsIndividualCommitSets(List<String> revisions) {
        ArrayList<Commits> commitSets = new ArrayList<>();
        for (String revision : revisions) {
            commitSets.add(Commits.from(new Commit(revision)));
        }
        return commitSets;
    }

    private ArrayList<CodeSceneBuildActionEntry> runDeltaAnalysesOnIndividualCommits(Configuration config, List<String> revisions, TaskListener listener) throws MalformedURLException {
        List<Commits> commitSets = revisionsAsIndividualCommitSets(revisions);
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

                entries.add(new CodeSceneBuildActionEntry(
                        commits.value().get(0).value(),
                        false,
                        commits.value(),
                        result.getRisk(),
                        result.getWarnings().value(),
                        detailsUrl));
            }
        } else {
            listener.getLogger().format("No commits to run delta analysis on.\n");
        }

        return entries;
    }

    private CodeSceneBuildActionEntry runDeltaAnalysisOnBranchDiff(Configuration config, List<String> revisions, String branchName, TaskListener listener) throws MalformedURLException {
        Commits commitSet = revisionsAsCommitSet(revisions);
        DeltaAnalysis deltaAnalysis = new DeltaAnalysis(config);
        listener.getLogger().format("Running delta analysis on branch %s in repository %s.\n", branchName, config.gitRepisitoryToAnalyze().value());
        DeltaAnalysisResult result = deltaAnalysis.runOn(commitSet);

        URL detailsUrl = new URL(
                config.codeSceneUrl().getProtocol(),
                config.codeSceneUrl().getHost(),
                config.codeSceneUrl().getPort(),
                result.getViewUrl());

        return new CodeSceneBuildActionEntry(branchName, true, commitSet.value(), result.getRisk(), result.getWarnings().value(), detailsUrl);
    }

    @Override
    public void perform(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener) {
        if (!getAnalyzeLatestIndividually() && !getAnalyzeBranchDiff()) {
            return;
        }

        try {
            URL codesceneUrl = new URL("http", "localhost", 3003, "/projects/13/delta-analysis");
            CodeSceneUser codeSceneUser = new CodeSceneUser("Foo", "Foo");
            Repository codeSceneGitRepository = new Repository("empear-enterprise");
            Configuration codesceneConfig = new Configuration(codesceneUrl, codeSceneUser, codeSceneGitRepository);
            EnvVars env = build.getEnvironment(listener);

            String previousCommit = env.get("GIT_PREVIOUS_SUCCESSFUL_COMMIT");
            String currentCommit = env.get("GIT_COMMIT");
            String branch = env.get("GIT_BRANCH");

            if (getAnalyzeLatestIndividually() && previousCommit != null) {
                List<String> revisions = getCommitRange(build, workspace, launcher, listener, previousCommit, currentCommit);
                if (revisions.isEmpty()) {
                    listener.getLogger().println("No new commits to analyze individually for this build.");
                } else {
                    ArrayList<CodeSceneBuildActionEntry> entries = runDeltaAnalysesOnIndividualCommits(codesceneConfig, revisions, listener);
                    build.addAction(new CodeSceneBuildAction("Delta - Individual Commits", entries));
                }
            }
            if (getAnalyzeBranchDiff() && getBaseRevision() != null) {
                List<String> revisions = getCommitRange(build, workspace, launcher, listener, getBaseRevision(), currentCommit);
                CodeSceneBuildActionEntry entry = runDeltaAnalysisOnBranchDiff(codesceneConfig, revisions, branch, listener);
                build.addAction(new CodeSceneBuildAction("Delta - By Branch", Arrays.asList(entry)));
            }

        } catch (InterruptedException e) {
            listener.error("Failed to run delta analysis", e);
        } catch (IOException e) {
            listener.error("Failed to run delta analysis", e);
        }
    }

    private List<String> getCommitRange(
            Run<?, ?> build,
            FilePath workspace,
            Launcher launcher,
            TaskListener listener,
            String fromRevision,
            String toRevision) throws IOException, InterruptedException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        launcher.launch()
                .cmdAsSingleString(String.format("git log --pretty='%%H' %s..%s", fromRevision, toRevision))
                .pwd(workspace)
                .envs(build.getEnvironment(listener))
                .stdout(out)
                .join();

        ArrayList<String> revisions = new ArrayList<>();
        for (String line : out.toString().split("\n")) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                revisions.add(trimmed);
            }
        }
        return revisions;
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

        public FormValidation doCheckBaseRevision(@QueryParameter boolean analyzeBranchDiff,
                                                  @QueryParameter String baseRevision) throws IOException, ServletException {
            if (analyzeBranchDiff && (baseRevision == null || baseRevision.isEmpty())) {
                return FormValidation.error("Base revision cannot be empty.");
            } else {
                return FormValidation.ok();
            }
        }
    }
}

