package java.util.logging;

import java.util.List;

public interface LoggingMXBean {
    String getLoggerLevel(String str);

    List<String> getLoggerNames();

    String getParentLoggerName(String str);

    void setLoggerLevel(String str, String str2);
}
