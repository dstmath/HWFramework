package org.junit.runner.notification;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import org.junit.runner.Description;

public class Failure implements Serializable {
    private static final long serialVersionUID = 1;
    private final Description fDescription;
    private final Throwable fThrownException;

    public Failure(Description description, Throwable thrownException) {
        this.fThrownException = thrownException;
        this.fDescription = description;
    }

    public String getTestHeader() {
        return this.fDescription.getDisplayName();
    }

    public Description getDescription() {
        return this.fDescription;
    }

    public Throwable getException() {
        return this.fThrownException;
    }

    public String toString() {
        return getTestHeader() + ": " + this.fThrownException.getMessage();
    }

    public String getTrace() {
        StringWriter stringWriter = new StringWriter();
        getException().printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }

    public String getMessage() {
        return getException().getMessage();
    }
}
