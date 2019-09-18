package java.util.logging;

import java.util.List;
import sun.util.logging.LoggingProxy;

class LoggingProxyImpl implements LoggingProxy {
    static final LoggingProxy INSTANCE = new LoggingProxyImpl();

    private LoggingProxyImpl() {
    }

    public Object getLogger(String name) {
        return Logger.getPlatformLogger(name);
    }

    public Object getLevel(Object logger) {
        return ((Logger) logger).getLevel();
    }

    public void setLevel(Object logger, Object newLevel) {
        ((Logger) logger).setLevel((Level) newLevel);
    }

    public boolean isLoggable(Object logger, Object level) {
        return ((Logger) logger).isLoggable((Level) level);
    }

    public void log(Object logger, Object level, String msg) {
        ((Logger) logger).log((Level) level, msg);
    }

    public void log(Object logger, Object level, String msg, Throwable t) {
        ((Logger) logger).log((Level) level, msg, t);
    }

    public void log(Object logger, Object level, String msg, Object... params) {
        ((Logger) logger).log((Level) level, msg, params);
    }

    public List<String> getLoggerNames() {
        return LogManager.getLoggingMXBean().getLoggerNames();
    }

    public String getLoggerLevel(String loggerName) {
        return LogManager.getLoggingMXBean().getLoggerLevel(loggerName);
    }

    public void setLoggerLevel(String loggerName, String levelName) {
        LogManager.getLoggingMXBean().setLoggerLevel(loggerName, levelName);
    }

    public String getParentLoggerName(String loggerName) {
        return LogManager.getLoggingMXBean().getParentLoggerName(loggerName);
    }

    public Object parseLevel(String levelName) {
        Level level = Level.findLevel(levelName);
        if (level != null) {
            return level;
        }
        throw new IllegalArgumentException("Unknown level \"" + levelName + "\"");
    }

    public String getLevelName(Object level) {
        return ((Level) level).getLevelName();
    }

    public int getLevelValue(Object level) {
        return ((Level) level).intValue();
    }

    public String getProperty(String key) {
        return LogManager.getLogManager().getProperty(key);
    }
}
