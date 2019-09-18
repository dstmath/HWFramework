package gov.nist.core;

import java.util.Properties;

public class LogWriter implements StackLogger {
    private static final String TAG = "SIP_STACK";
    private boolean mEnabled = true;

    public void logStackTrace() {
    }

    public void logStackTrace(int traceLevel) {
    }

    public int getLineCount() {
        return 0;
    }

    public void logException(Throwable ex) {
    }

    public void logDebug(String message) {
    }

    public void logTrace(String message) {
    }

    public void logFatalError(String message) {
    }

    public void logError(String message) {
    }

    public boolean isLoggingEnabled() {
        return this.mEnabled;
    }

    public boolean isLoggingEnabled(int logLevel) {
        return this.mEnabled;
    }

    public void logError(String message, Exception ex) {
    }

    public void logWarning(String string) {
    }

    public void logInfo(String string) {
    }

    public void disableLogging() {
        this.mEnabled = false;
    }

    public void enableLogging() {
        this.mEnabled = true;
    }

    public void setBuildTimeStamp(String buildTimeStamp) {
    }

    public void setStackProperties(Properties stackProperties) {
    }

    public String getLoggerName() {
        return "Android SIP Logger";
    }
}
