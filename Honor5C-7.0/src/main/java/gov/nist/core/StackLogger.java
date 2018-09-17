package gov.nist.core;

import java.util.Properties;

public interface StackLogger extends LogLevels {
    void disableLogging();

    void enableLogging();

    int getLineCount();

    String getLoggerName();

    boolean isLoggingEnabled();

    boolean isLoggingEnabled(int i);

    void logDebug(String str);

    void logError(String str);

    void logError(String str, Exception exception);

    void logException(Throwable th);

    void logFatalError(String str);

    void logInfo(String str);

    void logStackTrace();

    void logStackTrace(int i);

    void logTrace(String str);

    void logWarning(String str);

    void setBuildTimeStamp(String str);

    void setStackProperties(Properties properties);
}
