package android.view.textclassifier;

public final class Log {
    static final boolean ENABLE_FULL_LOGGING = android.util.Log.isLoggable(TextClassifier.DEFAULT_LOG_TAG, 2);

    private Log() {
    }

    public static void v(String tag, String msg) {
        if (ENABLE_FULL_LOGGING) {
            android.util.Log.v(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        android.util.Log.d(tag, msg);
    }

    public static void w(String tag, String msg) {
        android.util.Log.w(tag, msg);
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (ENABLE_FULL_LOGGING) {
            android.util.Log.e(tag, msg, tr);
        } else {
            android.util.Log.d(tag, String.format("%s (%s)", msg, tr != null ? tr.getClass().getSimpleName() : "??"));
        }
    }
}
