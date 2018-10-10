package org.jenkinsci.plugins.codescene.Domain;

/**
 * A custom exception in case the remote CodeScene analysis failed.
 * We need a separate error handling in this case since some users
 * do not want to fail their whole build pipeline.
 */
public class RemoteAnalysisException extends Exception {

    public RemoteAnalysisException(final String rootCause) {
        super(rootCause);
    }

    public RemoteAnalysisException(final String rootCause, final Exception rootException) {
        super(rootCause, rootException);
    }
}
