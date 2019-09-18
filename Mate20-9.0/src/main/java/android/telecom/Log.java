package android.telecom;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.provider.SettingsStringUtil;
import android.telecom.Logging.EventManager;
import android.telecom.Logging.Session;
import android.telecom.Logging.SessionManager;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Slog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.IndentingPrintWriter;
import java.util.IllegalFormatException;
import java.util.Locale;

public class Log {
    public static boolean DEBUG = isLoggable(3);
    public static boolean ERROR = isLoggable(6);
    private static final int EVENTS_TO_CACHE = 10;
    private static final int EVENTS_TO_CACHE_DEBUG = 20;
    private static final long EXTENDED_LOGGING_DURATION_MILLIS = 1800000;
    private static final boolean FORCE_LOGGING = false;
    public static boolean INFO = isLoggable(4);
    @VisibleForTesting
    public static String TAG = "TelecomFramework";
    private static final boolean USER_BUILD = Build.IS_USER;
    public static boolean VERBOSE = isLoggable(2);
    public static boolean WARN = isLoggable(5);
    private static EventManager sEventManager;
    private static boolean sIsUserExtendedLoggingEnabled = false;
    private static SessionManager sSessionManager;
    private static final Object sSingletonSync = new Object();
    private static long sUserExtendedLoggingStopTime = 0;

    private Log() {
    }

    public static void d(String prefix, String format, Object... args) {
        if (sIsUserExtendedLoggingEnabled) {
            maybeDisableLogging();
            Slog.i(TAG, buildMessage(prefix, format, args));
        } else if (DEBUG) {
            Slog.d(TAG, buildMessage(prefix, format, args));
        }
    }

    public static void d(Object objectPrefix, String format, Object... args) {
        if (sIsUserExtendedLoggingEnabled) {
            maybeDisableLogging();
            Slog.i(TAG, buildMessage(getPrefixFromObject(objectPrefix), format, args));
        } else if (DEBUG) {
            Slog.d(TAG, buildMessage(getPrefixFromObject(objectPrefix), format, args));
        }
    }

    public static void i(String prefix, String format, Object... args) {
        if (INFO) {
            Slog.i(TAG, buildMessage(prefix, format, args));
        }
    }

    public static void i(Object objectPrefix, String format, Object... args) {
        if (INFO) {
            Slog.i(TAG, buildMessage(getPrefixFromObject(objectPrefix), format, args));
        }
    }

    public static void v(String prefix, String format, Object... args) {
        if (sIsUserExtendedLoggingEnabled) {
            maybeDisableLogging();
            Slog.i(TAG, buildMessage(prefix, format, args));
        } else if (VERBOSE) {
            Slog.v(TAG, buildMessage(prefix, format, args));
        }
    }

    public static void v(Object objectPrefix, String format, Object... args) {
        if (sIsUserExtendedLoggingEnabled) {
            maybeDisableLogging();
            Slog.i(TAG, buildMessage(getPrefixFromObject(objectPrefix), format, args));
        } else if (VERBOSE) {
            Slog.v(TAG, buildMessage(getPrefixFromObject(objectPrefix), format, args));
        }
    }

    public static void w(String prefix, String format, Object... args) {
        if (WARN) {
            Slog.w(TAG, buildMessage(prefix, format, args));
        }
    }

    public static void w(Object objectPrefix, String format, Object... args) {
        if (WARN) {
            Slog.w(TAG, buildMessage(getPrefixFromObject(objectPrefix), format, args));
        }
    }

    public static void e(String prefix, Throwable tr, String format, Object... args) {
        if (ERROR) {
            Slog.e(TAG, buildMessage(prefix, format, args), tr);
        }
    }

    public static void e(Object objectPrefix, Throwable tr, String format, Object... args) {
        if (ERROR) {
            Slog.e(TAG, buildMessage(getPrefixFromObject(objectPrefix), format, args), tr);
        }
    }

    public static void wtf(String prefix, Throwable tr, String format, Object... args) {
        Slog.wtf(TAG, buildMessage(prefix, format, args), tr);
    }

    public static void wtf(Object objectPrefix, Throwable tr, String format, Object... args) {
        Slog.wtf(TAG, buildMessage(getPrefixFromObject(objectPrefix), format, args), tr);
    }

    public static void wtf(String prefix, String format, Object... args) {
        String msg = buildMessage(prefix, format, args);
        Slog.wtf(TAG, msg, new IllegalStateException(msg));
    }

    public static void wtf(Object objectPrefix, String format, Object... args) {
        String msg = buildMessage(getPrefixFromObject(objectPrefix), format, args);
        Slog.wtf(TAG, msg, new IllegalStateException(msg));
    }

    public static void setSessionContext(Context context) {
        getSessionManager().setContext(context);
    }

    public static void startSession(String shortMethodName) {
        getSessionManager().startSession(shortMethodName, null);
    }

    public static void startSession(Session.Info info, String shortMethodName) {
        getSessionManager().startSession(info, shortMethodName, null);
    }

    public static void startSession(String shortMethodName, String callerIdentification) {
        getSessionManager().startSession(shortMethodName, callerIdentification);
    }

    public static void startSession(Session.Info info, String shortMethodName, String callerIdentification) {
        getSessionManager().startSession(info, shortMethodName, callerIdentification);
    }

    public static Session createSubsession() {
        return getSessionManager().createSubsession();
    }

    public static Session.Info getExternalSession() {
        return getSessionManager().getExternalSession();
    }

    public static void cancelSubsession(Session subsession) {
        getSessionManager().cancelSubsession(subsession);
    }

    public static void continueSession(Session subsession, String shortMethodName) {
        getSessionManager().continueSession(subsession, shortMethodName);
    }

    public static void endSession() {
        getSessionManager().endSession();
    }

    public static void registerSessionListener(SessionManager.ISessionListener l) {
        getSessionManager().registerSessionListener(l);
    }

    public static String getSessionId() {
        synchronized (sSingletonSync) {
            if (sSessionManager == null) {
                return "";
            }
            String sessionId = getSessionManager().getSessionId();
            return sessionId;
        }
    }

    public static void addEvent(EventManager.Loggable recordEntry, String event) {
        getEventManager().event(recordEntry, event, null);
    }

    public static void addEvent(EventManager.Loggable recordEntry, String event, Object data) {
        getEventManager().event(recordEntry, event, data);
    }

    public static void addEvent(EventManager.Loggable recordEntry, String event, String format, Object... args) {
        getEventManager().event(recordEntry, event, format, args);
    }

    public static void registerEventListener(EventManager.EventListener e) {
        getEventManager().registerEventListener(e);
    }

    public static void addRequestResponsePair(EventManager.TimedEventPair p) {
        getEventManager().addRequestResponsePair(p);
    }

    public static void dumpEvents(IndentingPrintWriter pw) {
        synchronized (sSingletonSync) {
            if (sEventManager != null) {
                getEventManager().dumpEvents(pw);
            } else {
                pw.println("No Historical Events Logged.");
            }
        }
    }

    public static void dumpEventsTimeline(IndentingPrintWriter pw) {
        synchronized (sSingletonSync) {
            if (sEventManager != null) {
                getEventManager().dumpEventsTimeline(pw);
            } else {
                pw.println("No Historical Events Logged.");
            }
        }
    }

    public static void setIsExtendedLoggingEnabled(boolean isExtendedLoggingEnabled) {
        if (sIsUserExtendedLoggingEnabled != isExtendedLoggingEnabled) {
            if (sEventManager != null) {
                sEventManager.changeEventCacheSize(isExtendedLoggingEnabled ? 20 : 10);
            }
            sIsUserExtendedLoggingEnabled = isExtendedLoggingEnabled;
            if (sIsUserExtendedLoggingEnabled) {
                sUserExtendedLoggingStopTime = System.currentTimeMillis() + EXTENDED_LOGGING_DURATION_MILLIS;
            } else {
                sUserExtendedLoggingStopTime = 0;
            }
        }
    }

    private static EventManager getEventManager() {
        if (sEventManager == null) {
            synchronized (sSingletonSync) {
                if (sEventManager == null) {
                    sEventManager = new EventManager($$Lambda$qa4s1Fm2YuohEunaJUJcmJXDXG0.INSTANCE);
                    EventManager eventManager = sEventManager;
                    return eventManager;
                }
            }
        }
        return sEventManager;
    }

    @VisibleForTesting
    public static SessionManager getSessionManager() {
        if (sSessionManager == null) {
            synchronized (sSingletonSync) {
                if (sSessionManager == null) {
                    sSessionManager = new SessionManager();
                    SessionManager sessionManager = sSessionManager;
                    return sessionManager;
                }
            }
        }
        return sSessionManager;
    }

    public static void setTag(String tag) {
        TAG = tag;
        DEBUG = isLoggable(3);
        INFO = isLoggable(4);
        VERBOSE = isLoggable(2);
        WARN = isLoggable(5);
        ERROR = isLoggable(6);
    }

    private static void maybeDisableLogging() {
        if (sIsUserExtendedLoggingEnabled && sUserExtendedLoggingStopTime < System.currentTimeMillis()) {
            sUserExtendedLoggingStopTime = 0;
            sIsUserExtendedLoggingEnabled = false;
        }
    }

    public static boolean isLoggable(int level) {
        return android.util.Log.isLoggable(TAG, level);
    }

    public static String piiHandle(Object pii) {
        if (pii == null || VERBOSE) {
            return String.valueOf(pii);
        }
        StringBuilder sb = new StringBuilder();
        if (pii instanceof Uri) {
            Uri uri = (Uri) pii;
            String scheme = uri.getScheme();
            if (!TextUtils.isEmpty(scheme)) {
                sb.append(scheme);
                sb.append(SettingsStringUtil.DELIMITER);
            }
            String textToObfuscate = uri.getSchemeSpecificPart();
            int i = 0;
            if (PhoneAccount.SCHEME_TEL.equals(scheme)) {
                while (true) {
                    int i2 = i;
                    if (i2 >= textToObfuscate.length()) {
                        break;
                    }
                    char c = textToObfuscate.charAt(i2);
                    sb.append(PhoneNumberUtils.isDialable(c) ? "*" : Character.valueOf(c));
                    i = i2 + 1;
                }
            } else if ("sip".equals(scheme)) {
                while (true) {
                    int i3 = i;
                    if (i3 >= textToObfuscate.length()) {
                        break;
                    }
                    char c2 = textToObfuscate.charAt(i3);
                    if (!(c2 == '@' || c2 == '.')) {
                        c2 = '*';
                    }
                    sb.append(c2);
                    i = i3 + 1;
                }
            } else {
                sb.append(pii(pii));
            }
        }
        return sb.toString();
    }

    public static String pii(Object pii) {
        if (pii == null || VERBOSE) {
            return String.valueOf(pii);
        }
        return "***";
    }

    private static String getPrefixFromObject(Object obj) {
        return obj == null ? "<null>" : obj.getClass().getSimpleName();
    }

    private static String buildMessage(String prefix, String format, Object... args) {
        String sessionPostfix;
        String msg;
        if (TextUtils.isEmpty(getSessionId())) {
            sessionPostfix = "";
        } else {
            sessionPostfix = ": " + sessionName;
        }
        if (args != null) {
            try {
                if (args.length != 0) {
                    msg = String.format(Locale.US, format, args);
                    return String.format(Locale.US, "%s: %s%s", new Object[]{prefix, msg, sessionPostfix});
                }
            } catch (IllegalFormatException ife) {
                e(TAG, (Throwable) ife, "Log: IllegalFormatException: formatString='%s' numArgs=%d", format, Integer.valueOf(args.length));
                msg = format + " (An error occurred while formatting the message.)";
            }
        }
        msg = format;
        return String.format(Locale.US, "%s: %s%s", new Object[]{prefix, msg, sessionPostfix});
    }
}
