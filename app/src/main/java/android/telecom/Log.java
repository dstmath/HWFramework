package android.telecom;

import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.text.format.DateFormat;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.telephony.PhoneConstants;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

public final class Log {
    public static final boolean DEBUG = false;
    public static final boolean ERROR = false;
    public static final boolean FORCE_LOGGING = true;
    public static final boolean INFO = false;
    private static final String TAG = "TelecomFramework";
    public static final boolean VERBOSE = false;
    public static final boolean WARN = false;
    private static MessageDigest sMessageDigest;
    private static final Object sMessageDigestLock = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.telecom.Log.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.telecom.Log.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.telecom.Log.<clinit>():void");
    }

    private Log() {
    }

    public static void initMd5Sum() {
        new AsyncTask<Void, Void, Void>() {
            public Void doInBackground(Void... args) {
                MessageDigest instance;
                try {
                    instance = MessageDigest.getInstance("SHA-1");
                } catch (NoSuchAlgorithmException e) {
                    instance = null;
                }
                synchronized (Log.sMessageDigestLock) {
                    Log.sMessageDigest = instance;
                }
                return null;
            }
        }.execute(new Void[0]);
    }

    public static boolean isLoggable(int level) {
        return FORCE_LOGGING;
    }

    public static void d(String prefix, String format, Object... args) {
        if (DEBUG) {
            android.util.Log.d(TAG, buildMessage(prefix, format, args));
        }
    }

    public static void d(Object objectPrefix, String format, Object... args) {
        if (DEBUG) {
            android.util.Log.d(TAG, buildMessage(getPrefixFromObject(objectPrefix), format, args));
        }
    }

    public static void i(String prefix, String format, Object... args) {
        if (INFO) {
            android.util.Log.i(TAG, buildMessage(prefix, format, args));
        }
    }

    public static void i(Object objectPrefix, String format, Object... args) {
        if (INFO) {
            android.util.Log.i(TAG, buildMessage(getPrefixFromObject(objectPrefix), format, args));
        }
    }

    public static void v(String prefix, String format, Object... args) {
        if (VERBOSE) {
            android.util.Log.v(TAG, buildMessage(prefix, format, args));
        }
    }

    public static void v(Object objectPrefix, String format, Object... args) {
        if (VERBOSE) {
            android.util.Log.v(TAG, buildMessage(getPrefixFromObject(objectPrefix), format, args));
        }
    }

    public static void w(String prefix, String format, Object... args) {
        if (WARN) {
            android.util.Log.w(TAG, buildMessage(prefix, format, args));
        }
    }

    public static void w(Object objectPrefix, String format, Object... args) {
        if (WARN) {
            android.util.Log.w(TAG, buildMessage(getPrefixFromObject(objectPrefix), format, args));
        }
    }

    public static void e(String prefix, Throwable tr, String format, Object... args) {
        if (ERROR) {
            android.util.Log.e(TAG, buildMessage(prefix, format, args), tr);
        }
    }

    public static void e(Object objectPrefix, Throwable tr, String format, Object... args) {
        if (ERROR) {
            android.util.Log.e(TAG, buildMessage(getPrefixFromObject(objectPrefix), format, args), tr);
        }
    }

    public static void wtf(String prefix, Throwable tr, String format, Object... args) {
        android.util.Log.wtf(TAG, buildMessage(prefix, format, args), tr);
    }

    public static void wtf(Object objectPrefix, Throwable tr, String format, Object... args) {
        android.util.Log.wtf(TAG, buildMessage(getPrefixFromObject(objectPrefix), format, args), tr);
    }

    public static void wtf(String prefix, String format, Object... args) {
        String msg = buildMessage(prefix, format, args);
        android.util.Log.wtf(TAG, msg, new IllegalStateException(msg));
    }

    public static void wtf(Object objectPrefix, String format, Object... args) {
        String msg = buildMessage(getPrefixFromObject(objectPrefix), format, args);
        android.util.Log.wtf(TAG, msg, new IllegalStateException(msg));
    }

    public static String pii(Object pii) {
        if (pii == null || VERBOSE) {
            return String.valueOf(pii);
        }
        if (pii instanceof Uri) {
            return piiUri((Uri) pii);
        }
        return "[" + secureHash(String.valueOf(pii).getBytes()) + "]";
    }

    private static String piiUri(Uri handle) {
        StringBuilder sb = new StringBuilder();
        String scheme = handle.getScheme();
        if (!TextUtils.isEmpty(scheme)) {
            sb.append(scheme).append(":");
        }
        String value = handle.getSchemeSpecificPart();
        if (!TextUtils.isEmpty(value)) {
            for (int i = 0; i < value.length(); i++) {
                char c = value.charAt(i);
                if (PhoneNumberUtils.isStartsPostDial(c)) {
                    sb.append(c);
                } else if (PhoneNumberUtils.isDialable(c)) {
                    sb.append(PhoneConstants.APN_TYPE_ALL);
                } else if ((DateFormat.AM_PM > c || c > DateFormat.TIME_ZONE) && (DateFormat.CAPITAL_AM_PM > c || c > 'Z')) {
                    sb.append(c);
                } else {
                    sb.append(PhoneConstants.APN_TYPE_ALL);
                }
            }
        }
        return sb.toString();
    }

    private static String secureHash(byte[] input) {
        synchronized (sMessageDigestLock) {
            if (sMessageDigest != null) {
                sMessageDigest.reset();
                sMessageDigest.update(input);
                String encodeHex = encodeHex(sMessageDigest.digest());
                return encodeHex;
            }
            encodeHex = "Uninitialized SHA1";
            return encodeHex;
        }
    }

    private static String encodeHex(byte[] bytes) {
        StringBuffer hex = new StringBuffer(bytes.length * 2);
        for (byte b : bytes) {
            int byteIntValue = b & MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE;
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
        if (args != null) {
            try {
                if (args.length != 0) {
                    msg = String.format(Locale.US, format, args);
                    return String.format(Locale.US, "%s: %s", new Object[]{prefix, msg});
                }
            } catch (Throwable ife) {
                wtf("Log", ife, "IllegalFormatException: formatString='%s' numArgs=%d", format, Integer.valueOf(args.length));
                msg = format + " (An error occurred while formatting the message.)";
            }
        }
        msg = format;
        return String.format(Locale.US, "%s: %s", new Object[]{prefix, msg});
    }
}
