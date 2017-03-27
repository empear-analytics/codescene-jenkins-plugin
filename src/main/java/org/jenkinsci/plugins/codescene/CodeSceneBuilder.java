package org.jenkinsci.plugins.codescene;
import hudson.EnvVars;
import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.util.FormValidation;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class CodeSceneBuilder extends Builder implements SimpleBuildStep {

    private final String baseRevision;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public CodeSceneBuilder(String baseRevision) {
        this.baseRevision = baseRevision;
    }

    /**
     * We'll use this from the {@code config.jelly}.
     */
    public String getBaseRevision() {
        return baseRevision;
    }

    @Override
    public void perform(Run<?,?> build, FilePath workspace, Launcher launcher, TaskListener listener) {
        // This is where you 'build' the project.
        // Since this is a dummy, we just say 'hello world' and call that a build.


        try {
            EnvVars env = build.getEnvironment(listener);
            listener.getLogger().println(workspace);

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            int code = launcher.launch()
                    .cmdAsSingleString(String.format("git log --pretty='%%H' %s..%s", getBaseRevision(), env.get("GIT_COMMIT")))
                    .pwd(workspace)
                    .envs(build.getEnvironment(listener))
                    .stdout(out)
                    .join();

            listener.getLogger().println("Exit: " + code);
            listener.getLogger().println(out.toString());

        } catch (InterruptedException e) {
            listener.error("Failed to run git command", e);
        } catch (IOException e) {
            listener.error("Failed to run git command", e);
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

