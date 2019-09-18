package dalvik.system;

public final class DalvikLogging {
    private DalvikLogging() {
    }

    public static String loggerNameToTag(String loggerName) {
        String str;
        if (loggerName == null) {
            return "null";
        }
        int length = loggerName.length();
        if (length <= 23) {
            return loggerName;
        }
        int lastPeriod = loggerName.lastIndexOf(".");
        if (length - (lastPeriod + 1) <= 23) {
            str = loggerName.substring(lastPeriod + 1);
        } else {
            str = loggerName.substring(loggerName.length() - 23);
        }
        return str;
    }
}
