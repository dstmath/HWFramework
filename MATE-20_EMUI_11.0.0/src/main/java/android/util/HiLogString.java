package android.util;

public final class HiLogString {
    public static final String PRIVATE_STRING = "<private>";

    public static String format(boolean isFmtStrPrivate, String format, Object... args) {
        boolean showPrivacy = HiLog.isDebuggable();
        if (!showPrivacy && isFmtStrPrivate) {
            return PRIVATE_STRING;
        }
        try {
            return new HiLogFormatter().format(showPrivacy, format, args).toString();
        } catch (IllegalArgumentException | IllegalStateException | IndexOutOfBoundsException e) {
            HiLogFormatter hiLogFormatter = new HiLogFormatter();
            return hiLogFormatter.format(showPrivacy, "%{public}s", format + " is an illegal format").toString();
        }
    }
}
