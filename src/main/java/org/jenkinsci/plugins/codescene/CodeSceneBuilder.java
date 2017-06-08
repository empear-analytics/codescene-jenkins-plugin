package org.jenkinsci.plugins.codescene;

import static java.util.Collections.singletonList;

import hudson.EnvVars;
import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.console.HyperlinkNote;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import hudson.util.FormValidation;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.codescene.Domain.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CodeSceneBuilder extends Builder implements SimpleBuildStep {
    private static final int DEFAULT_RISK_THRESHOLD = 7;

    // required params
    private final String username;
    private final String password;
    private final String deltaAnalysisUrl;
    private final String repository;

    // optional params
    private boolean analyzeLatestIndividually;
    private boolean analyzeBranchDiff;
    private String baseRevision;
    private boolean markBuildAsUnstable;
    private int riskThreshold = DEFAULT_RISK_THRESHOLD;


    @DataBoundConstructor
    public CodeSceneBuilder(String username, String password, String deltaAnalysisUrl, String repository) {
        this.username = username;
        this.password = password;
        this.deltaAnalysisUrl = deltaAnalysisUrl;
        this.repository = repository;
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

    public boolean getMarkBuildAsUnstable() {
        return markBuildAsUnstable;
    }

    public int getRiskThreshold() {
        return riskThreshold;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDeltaAnalysisUrl() {
        return deltaAnalysisUrl;
    }

    public String getRepository() {
        return repository;
    }

    @DataBoundSetter
    public void setAnalyzeLatestIndividually(boolean analyzeLatestIndividually) {
        this.analyzeLatestIndividually = analyzeLatestIndividually;
    }

    @DataBoundSetter
    public void setAnalyzeBranchDiff(boolean analyzeBranchDiff) {
        this.analyzeBranchDiff = analyzeBranchDiff;
    }

    @DataBoundSetter
    public void setBaseRevision(String baseRevision) {
        this.baseRevision = baseRevision;
    }

    @DataBoundSetter
    public void setMarkBuildAsUnstable(boolean markBuildAsUnstable) {
        this.markBuildAsUnstable = markBuildAsUnstable;
    }

    @DataBoundSetter
    public void setRiskThreshold(int riskThreshold) {
        this.riskThreshold = riskThreshold < 1 || riskThreshold > 10 ? DEFAULT_RISK_THRESHOLD : riskThreshold;
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
            listener.getLogger().format("Starting delta analysis on %d commit(s)...%n", commitSets.size());
            for (Commits commits : commitSets) {
                DeltaAnalysis deltaAnalysis = new DeltaAnalysis(config);
                listener.getLogger().format("Running delta analysis on commits (%s) in repository %s.%n", commits.value(), config.gitRepisitoryToAnalyze().value());
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
                        detailsUrl,
                        riskThreshold));
            }
        } else {
            listener.getLogger().format("No commits to run delta analysis on.%n");
        }

        return entries;
    }

    private CodeSceneBuildActionEntry runDeltaAnalysisOnBranchDiff(Configuration config, List<String> revisions, String branchName, TaskListener listener) throws MalformedURLException {
        Commits commitSet = revisionsAsCommitSet(revisions);
        DeltaAnalysis deltaAnalysis = new DeltaAnalysis(config);
        listener.getLogger().format("Running delta analysis on branch %s in repository %s.%n", branchName, config.gitRepisitoryToAnalyze().value());
        DeltaAnalysisResult result = deltaAnalysis.runOn(commitSet);

        URL detailsUrl = new URL(
                config.codeSceneUrl().getProtocol(),
                config.codeSceneUrl().getHost(),
                config.codeSceneUrl().getPort(),
                result.getViewUrl());

        return new CodeSceneBuildActionEntry(branchName, true, commitSet.value(), result.getRisk(), result.getWarnings().value(), detailsUrl, riskThreshold);
    }

    @Override
    public void perform(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener) {
        if (!getAnalyzeLatestIndividually() && !getAnalyzeBranchDiff()) {
            return;
        }

        try {
            URL url = new URL(deltaAnalysisUrl);
            Configuration codesceneConfig = new Configuration(url, new CodeSceneUser(username, password), new Repository(repository));
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
                    for (CodeSceneBuildActionEntry entry : entries) {
                        markAsUnstableWhenAtRiskThreshold(riskThreshold, entry, build, listener);
                    }
                    build.addAction(new CodeSceneBuildAction("Delta - Individual Commits", entries));
                }
            }
            if (getAnalyzeBranchDiff() && getBaseRevision() != null) {
                List<String> revisions = getCommitRange(build, workspace, launcher, listener, getBaseRevision(), currentCommit);
                CodeSceneBuildActionEntry entry = runDeltaAnalysisOnBranchDiff(codesceneConfig, revisions, branch, listener);
                markAsUnstableWhenAtRiskThreshold(riskThreshold, entry, build, listener);
                build.addAction(new CodeSceneBuildAction("Delta - By Branch", singletonList(entry)));
            }

        } catch (InterruptedException | IOException e) {
            listener.error("Failed to run delta analysis: %s", e);
            build.setResult(Result.FAILURE);
        }
    }

    private void markAsUnstableWhenAtRiskThreshold(int threshold, CodeSceneBuildActionEntry entry, Run<?, ?> build, TaskListener listener) throws IOException {
        if (getMarkBuildAsUnstable() && entry.getHitsRiskThreshold()) {
            String link = HyperlinkNote.encodeTo(entry.getViewUrl().toExternalForm(), String.format("Delta analysis result with risk %d", entry.getRisk().getValue()));
            listener.error("%s hits the risk threshold (%d). Marking build as unstable.", link, threshold);
            Result newResult = Result.UNSTABLE;

            Result result = build.getResult();
            if (result != null) {
                build.setResult(result.combine(newResult));
            } else {
                build.setResult(newResult);
            }
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
        for (String line : out.toString("UTF8").split("\n")) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                revisions.add(trimmed);
            }
        }
        return revisions;
    }

    @Symbol("codescene")
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

        public FormValidation doCheckRiskThreshold(@QueryParameter int riskThreshold, @QueryParameter boolean markBuildAsUnstable) {
            if (markBuildAsUnstable && (riskThreshold < 1 || riskThreshold > 10)) {
                return FormValidation.error("Risk threshold must be a number between 1 and 10. The value %d is invalid.", riskThreshold);
            } else {
                return FormValidation.ok();
            }
        }

        public FormValidation doCheckUsername(@QueryParameter String username) {
            if (username == null || username.isEmpty()) {
                return FormValidation.error("CodeScene bot user name cannot be blank.");
            } else {
                return FormValidation.ok();
            }
        }

        public FormValidation doCheckPassword(@QueryParameter String password) {
            if (password == null || password.isEmpty()) {
                return FormValidation.error("CodeScene bot password cannot be blank.");
            } else {
                return FormValidation.ok();
            }
        }

        public FormValidation doCheckDeltaAnalysisUrl(@QueryParameter String deltaAnalysisUrl) {
            if (deltaAnalysisUrl == null || deltaAnalysisUrl.isEmpty()) {
                return FormValidation.error("CodeScene delta analysis URL cannot be blank.");
            } else {
                try {
                    new URL(deltaAnalysisUrl);
                    return FormValidation.ok();
                } catch (MalformedURLException e) {
                    return FormValidation.error("Invalid URL");
                }
            }
        }

        public FormValidation doCheckRepository(@QueryParameter String repository) {
            if (repository == null || repository.isEmpty()) {
                return FormValidation.error("CodeScene repository cannot be blank.");
            } else {
                return FormValidation.ok();
            }
        }
    }
}

