package org.apache.commons.logging.impl;

import java.io.Serializable;
import org.apache.commons.logging.Log;

@Deprecated
public class NoOpLog implements Log, Serializable {
    public NoOpLog(String name) {
    }

    public void trace(Object message) {
    }

    public void trace(Object message, Throwable t) {
    }

    public void debug(Object message) {
    }

    public void debug(Object message, Throwable t) {
    }

    public void info(Object message) {
    }

    public void info(Object message, Throwable t) {
    }

    public void warn(Object message) {
    }

    public void warn(Object message, Throwable t) {
    }

    public void error(Object message) {
    }

    public void error(Object message, Throwable t) {
    }

    public void fatal(Object message) {
    }

    public void fatal(Object message, Throwable t) {
    }

    public final boolean isDebugEnabled() {
        return false;
    }

    public final boolean isErrorEnabled() {
        return false;
    }

    public final boolean isFatalEnabled() {
        return false;
    }

    public final boolean isInfoEnabled() {
        return false;
    }

    public final boolean isTraceEnabled() {
        return false;
    }

    public final boolean isWarnEnabled() {
        return false;
    }
}
