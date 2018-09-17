package android.telecom;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.SettingsStringUtil;
import android.security.keystore.KeyProperties;
import android.telecom.Logging.EventManager;
import android.telecom.Logging.EventManager.EventListener;
import android.telecom.Logging.EventManager.Loggable;
import android.telecom.Logging.EventManager.TimedEventPair;
import android.telecom.Logging.Session;
import android.telecom.Logging.Session.Info;
import android.telecom.Logging.SessionManager;
import android.telecom.Logging.SessionManager.ISessionListener;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.LogException;
import android.util.Slog;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.util.IndentingPrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

public class Log {
    public static boolean DEBUG = isLoggable(3);
    public static boolean ERROR = isLoggable(6);
    private static final int EVENTS_TO_CACHE = 10;
    private static final int EVENTS_TO_CACHE_DEBUG = 20;
    private static final long EXTENDED_LOGGING_DURATION_MILLIS = 1800000;
    private static final boolean FORCE_LOGGING = false;
    public static boolean INFO = isLoggable(4);
    public static String TAG = "TelecomFramework";
    private static final boolean USER_BUILD = Build.TYPE.equals("user");
    public static boolean VERBOSE = isLoggable(2);
    public static boolean WARN = isLoggable(5);
    private static EventManager sEventManager;
    private static boolean sIsUserExtendedLoggingEnabled = false;
    private static MessageDigest sMessageDigest;
    private static final Object sMessageDigestLock = new Object();
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

    public static void startSession(Info info, String shortMethodName) {
        getSessionManager().startSession(info, shortMethodName, null);
    }

    public static void startSession(String shortMethodName, String callerIdentification) {
        getSessionManager().startSession(shortMethodName, callerIdentification);
    }

    public static void startSession(Info info, String shortMethodName, String callerIdentification) {
        getSessionManager().startSession(info, shortMethodName, callerIdentification);
    }

    public static Session createSubsession() {
        return getSessionManager().createSubsession();
    }

    public static Info getExternalSession() {
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

    public static void registerSessionListener(ISessionListener l) {
        getSessionManager().registerSessionListener(l);
    }

    public static String getSessionId() {
        synchronized (sSingletonSync) {
            String sessionId;
            if (sSessionManager != null) {
                sessionId = getSessionManager().getSessionId();
                return sessionId;
            }
            sessionId = LogException.NO_VALUE;
            return sessionId;
        }
    }

    public static void addEvent(Loggable recordEntry, String event) {
        getEventManager().event(recordEntry, event, null);
    }

    public static void addEvent(Loggable recordEntry, String event, Object data) {
        getEventManager().event(recordEntry, event, data);
    }

    public static void addEvent(Loggable recordEntry, String event, String format, Object... args) {
        getEventManager().event(recordEntry, event, format, args);
    }

    public static void registerEventListener(EventListener e) {
        getEventManager().registerEventListener(e);
    }

    public static void addRequestResponsePair(TimedEventPair p) {
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
                    sEventManager = new EventManager(new -$Lambda$afyb_ODGzn3xMew6fjs8ANSIdVo());
                    EventManager eventManager = sEventManager;
                    return eventManager;
                }
            }
        }
        return sEventManager;
    }

    private static SessionManager getSessionManager() {
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

    public static void initMd5Sum() {
        new AsyncTask<Void, Void, Void>() {
            public Void doInBackground(Void... args) {
                MessageDigest md;
                try {
                    md = MessageDigest.getInstance(KeyProperties.DIGEST_SHA1);
                } catch (NoSuchAlgorithmException e) {
                    md = null;
                }
                synchronized (Log.sMessageDigestLock) {
                    Log.sMessageDigest = md;
                }
                return null;
            }
        }.execute(new Void[0]);
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
                sb.append(scheme).append(SettingsStringUtil.DELIMITER);
            }
            String textToObfuscate = uri.getSchemeSpecificPart();
            int i;
            char c;
            if (PhoneAccount.SCHEME_TEL.equals(scheme)) {
                for (i = 0; i < textToObfuscate.length(); i++) {
                    c = textToObfuscate.charAt(i);
                    sb.append(PhoneNumberUtils.isDialable(c) ? PhoneConstants.APN_TYPE_ALL : Character.valueOf(c));
                }
            } else if ("sip".equals(scheme)) {
                for (i = 0; i < textToObfuscate.length(); i++) {
                    c = textToObfuscate.charAt(i);
                    if (!(c == '@' || c == '.')) {
                        c = '*';
                    }
                    sb.append(c);
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
        return "[" + secureHash(String.valueOf(pii).getBytes()) + "]";
    }

    private static String secureHash(byte[] input) {
        if (USER_BUILD) {
            return "****";
        }
        synchronized (sMessageDigestLock) {
            String encodeHex;
            if (sMessageDigest != null) {
                sMessageDigest.reset();
                sMessageDigest.update(input);
                encodeHex = encodeHex(sMessageDigest.digest());
                return encodeHex;
            }
            encodeHex = "Uninitialized SHA1";
            return encodeHex;
        }
    }

    private static String encodeHex(byte[] bytes) {
        StringBuffer hex = new StringBuffer(bytes.length * 2);
        for (byte b : bytes) {
            int byteIntValue = b & 255;
            if (byteIntValue < 16) {
                hex.append("0");
            }
            hex.append(Integer.toString(byteIntValue, 16));
        }
        return hex.toString();
    }

    private static String getPrefixFromObject(Object obj) {
        return obj == null ? "<null>" : obj.getClass().getSimpleName();
    }

    private static String buildMessage(String prefix, String format, Object... args) {
        String msg;
        String sessionName = getSessionId();
        String sessionPostfix = TextUtils.isEmpty(sessionName) ? LogException.NO_VALUE : ": " + sessionName;
        if (args != null) {
            try {
                if (args.length != 0) {
                    msg = String.format(Locale.US, format, args);
                    return String.format(Locale.US, "%s: %s%s", new Object[]{prefix, msg, sessionPostfix});
                }
            } catch (Throwable ife) {
                e(TAG, ife, "Log: IllegalFormatException: formatString='%s' numArgs=%d", format, Integer.valueOf(args.length));
                msg = format + " (An error occurred while formatting the message.)";
            }
        }
        msg = format;
        return String.format(Locale.US, "%s: %s%s", new Object[]{prefix, msg, sessionPostfix});
    }
}
