package org.jboss.aerogear.test.cli.exception;

public class GithubRepositoryException extends Exception {

    private static final long serialVersionUID = -8813935945734753554L;

    public GithubRepositoryException() {
        super();
    }

    public GithubRepositoryException(String message) {
        super(message);
    }

    public GithubRepositoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public GithubRepositoryException(Throwable cause) {
        super(cause);
    }

}
