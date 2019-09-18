package android.view.textclassifier;

import android.util.Slog;

final class Log {
    private static final boolean ENABLE_FULL_LOGGING = false;

    private Log() {
    }

    public static void d(String tag, String msg) {
        Slog.d(tag, msg);
    }

    public static void w(String tag, String msg) {
        Slog.w(tag, msg);
    }

    public static void e(String tag, String msg, Throwable tr) {
        Slog.d(tag, String.format("%s (%s)", new Object[]{msg, tr != null ? tr.getClass().getSimpleName() : "??"}));
    }
}
