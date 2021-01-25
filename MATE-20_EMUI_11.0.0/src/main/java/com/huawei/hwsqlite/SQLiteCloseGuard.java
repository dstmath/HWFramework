package com.huawei.hwsqlite;

import android.util.Log;

public final class SQLiteCloseGuard {
    private static final SQLiteCloseGuard NOOP = new SQLiteCloseGuard();
    private static volatile Reporter defaultReporter = new DefaultReporter();
    private static volatile boolean enabledGuard = true;
    private Throwable allocationSite;
    private String detailMessage;

    public interface Reporter {
        void report(String str, String str2);

        void report(String str, Throwable th);
    }

    private SQLiteCloseGuard() {
    }

    public static SQLiteCloseGuard get() {
        if (!enabledGuard) {
            return NOOP;
        }
        return new SQLiteCloseGuard();
    }

    public static void setEnabled(boolean enabled) {
        enabledGuard = enabled;
    }

    public static void setReporter(Reporter reporter) {
        if (reporter != null) {
            defaultReporter = reporter;
            return;
        }
        throw new NullPointerException("reporter == null");
    }

    public static Reporter getReporter() {
        return defaultReporter;
    }

    public void open(String closer) {
        if (closer == null) {
            throw new NullPointerException("closer == null");
        } else if (this != NOOP && enabledGuard) {
            this.allocationSite = new Throwable("Explicit termination method '" + cleanString(closer) + "' not called");
        }
    }

    public void open(String detail, boolean fillStack) {
        if (fillStack) {
            open(detail);
        } else if (detail == null) {
            throw new NullPointerException("detail == null");
        } else if (this != NOOP && enabledGuard) {
            this.detailMessage = detail;
        }
    }

    public void close() {
        this.allocationSite = null;
        this.detailMessage = null;
    }

    public void warnIfOpen() {
        if ((this.allocationSite == null && this.detailMessage == null) || !enabledGuard) {
            return;
        }
        if (this.detailMessage != null) {
            defaultReporter.report("A resource was acquired but never released", cleanString(this.detailMessage));
        } else {
            defaultReporter.report("A resource was acquired at attached stack trace but never released. See java.io.Closeable for information on avoiding resource leaks.", this.allocationSite);
        }
    }

    private static final class DefaultReporter implements Reporter {
        private DefaultReporter() {
        }

        @Override // com.huawei.hwsqlite.SQLiteCloseGuard.Reporter
        public void report(String message, Throwable allocationSite) {
            Log.w(SQLiteCloseGuard.cleanString(message), allocationSite);
        }

        @Override // com.huawei.hwsqlite.SQLiteCloseGuard.Reporter
        public void report(String message, String detail) {
            Log.w(SQLiteCloseGuard.cleanString(message), SQLiteCloseGuard.cleanString(detail));
        }
    }

    /* access modifiers changed from: private */
    public static String cleanString(String dirtyString) {
        return dirtyString.replaceAll("[^0-9a-zA-Z,.:';]J*", " ");
    }
}
