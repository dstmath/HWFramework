package dalvik.system;

public final class DalvikLogging {
    private DalvikLogging() {
    }

    public static String loggerNameToTag(String loggerName) {
        if (loggerName == null) {
            return "null";
        }
        int length = loggerName.length();
        if (length <= 23) {
            return loggerName;
        }
        String substring;
        int lastPeriod = loggerName.lastIndexOf(".");
        if (length - (lastPeriod + 1) <= 23) {
            substring = loggerName.substring(lastPeriod + 1);
        } else {
            substring = loggerName.substring(loggerName.length() - 23);
        }
        return substring;
    }
}
