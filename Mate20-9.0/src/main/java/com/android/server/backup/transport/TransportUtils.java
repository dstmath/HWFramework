package com.android.server.backup.transport;

import android.util.Log;
import android.util.Slog;
import com.android.internal.backup.IBackupTransport;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class TransportUtils {
    private static final String TAG = "TransportUtils";

    @Retention(RetentionPolicy.SOURCE)
    @interface Priority {
        public static final int DEBUG = 3;
        public static final int ERROR = 6;
        public static final int INFO = 4;
        public static final int VERBOSE = 2;
        public static final int WARN = 5;
        public static final int WTF = -1;
    }

    public static IBackupTransport checkTransportNotNull(IBackupTransport transport) throws TransportNotAvailableException {
        if (transport != null) {
            return transport;
        }
        log(6, TAG, "Transport not available");
        throw new TransportNotAvailableException();
    }

    static void log(int priority, String tag, String message) {
        if (priority == -1) {
            Slog.wtf(tag, message);
        } else if (Log.isLoggable(tag, priority)) {
            Slog.println(priority, tag, message);
        }
    }

    static String formatMessage(String prefix, String caller, String message) {
        StringBuilder string = new StringBuilder();
        if (prefix != null) {
            string.append(prefix);
            string.append(" ");
        }
        if (caller != null) {
            string.append("[");
            string.append(caller);
            string.append("] ");
        }
        string.append(message);
        return string.toString();
    }

    private TransportUtils() {
    }
}
