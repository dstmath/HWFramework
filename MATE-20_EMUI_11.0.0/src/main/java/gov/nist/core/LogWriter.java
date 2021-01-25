package gov.nist.core;

import java.util.Properties;

public class LogWriter implements StackLogger {
    private static final String TAG = "SIP_STACK";
    private boolean mEnabled = true;

    @Override // gov.nist.core.StackLogger
    public void logStackTrace() {
    }

    @Override // gov.nist.core.StackLogger
    public void logStackTrace(int traceLevel) {
    }

    @Override // gov.nist.core.StackLogger
    public int getLineCount() {
        return 0;
    }

    @Override // gov.nist.core.StackLogger
    public void logException(Throwable ex) {
    }

    @Override // gov.nist.core.StackLogger
    public void logDebug(String message) {
    }

    @Override // gov.nist.core.StackLogger
    public void logTrace(String message) {
    }

    @Override // gov.nist.core.StackLogger
    public void logFatalError(String message) {
    }

    @Override // gov.nist.core.StackLogger
    public void logError(String message) {
    }

    @Override // gov.nist.core.StackLogger
    public boolean isLoggingEnabled() {
        return this.mEnabled;
    }

    @Override // gov.nist.core.StackLogger
    public boolean isLoggingEnabled(int logLevel) {
        return this.mEnabled;
    }

    @Override // gov.nist.core.StackLogger
    public void logError(String message, Exception ex) {
    }

    @Override // gov.nist.core.StackLogger
    public void logWarning(String string) {
    }

    @Override // gov.nist.core.StackLogger
    public void logInfo(String string) {
    }

    @Override // gov.nist.core.StackLogger
    public void disableLogging() {
        this.mEnabled = false;
    }

    @Override // gov.nist.core.StackLogger
    public void enableLogging() {
        this.mEnabled = true;
    }

    @Override // gov.nist.core.StackLogger
    public void setBuildTimeStamp(String buildTimeStamp) {
    }

    @Override // gov.nist.core.StackLogger
    public void setStackProperties(Properties stackProperties) {
    }

    @Override // gov.nist.core.StackLogger
    public String getLoggerName() {
        return "Android SIP Logger";
    }
}
