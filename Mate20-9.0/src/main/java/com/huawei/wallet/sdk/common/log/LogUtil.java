package com.huawei.wallet.sdk.common.log;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.wallet.sdk.common.log.Logger;
import com.huawei.wallet.sdk.common.utils.crypto.AES;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

public class LogUtil {
    private static final int DEFAULT_APP_LOG_FILE_SIZE = 1024;
    private static final String GET_HW_LOG = "[getHWLog]: ";
    private static final boolean ISLOG_D = false;
    private static boolean IS_DEBUG_LOG_ENALBE = false;
    private static final boolean IS_HW_LOG = getHWLog();
    private static final int LEN_CONST = 2;
    private static final int LOG_BUF_LENGTH = 2048;
    private static final String METADATA = "MetaData";
    private static String PKG_NAME = null;
    private static final char STAR = '*';
    private static final byte[] SYNC_LOG_LOCK = new byte[0];
    private static final String TAB_STR = "    ";
    public static final String TAG = "walletSDK";
    private static final int TAG_CUSTOM_MAX_LEN = 16;
    private static final int TAG_MAX_LEN = 23;
    private static String VER_NAME;
    private static boolean hasAgreeLicense;
    private static int length = 0;
    private static int location = 0;
    private static byte[] logBuffer = new byte[LOG_BUF_LENGTH];
    private static String logBuildType;
    private static Context mContext;
    private static final Pattern mPattern = Pattern.compile("[0-9]*[a-z|A-Z]*[一-龥]*");

    private static int addLoc(int i) {
        if (i + 1 == LOG_BUF_LENGTH) {
            return 0;
        }
        return i + 1;
    }

    private static void put(byte z) {
        logBuffer[location] = z;
        location = addLoc(location);
        int i = length;
        int i2 = LOG_BUF_LENGTH;
        if (i != LOG_BUF_LENGTH) {
            i2 = length + 1;
        }
        length = i2;
    }

    public static void putLog(String log) {
        String time = new SimpleDateFormat("MM-dd HH:mm:ss.SSS").format(new Date());
        byte[] logByte = new byte[0];
        try {
            logByte = (time + " " + log + 13 + 10).getBytes(AES.CHAR_ENCODING);
        } catch (UnsupportedEncodingException e) {
            Log.w(TAG, "putStr() UnsupportedEncodingException");
        }
        synchronized (SYNC_LOG_LOCK) {
            for (byte put : logByte) {
                put(put);
            }
        }
    }

    public static String toSting() {
        if (length <= 0) {
            return "";
        }
        try {
            byte[] bArr = logBuffer;
            int i = length;
            int i2 = LOG_BUF_LENGTH;
            if (i <= LOG_BUF_LENGTH) {
                i2 = length;
            }
            return new String(bArr, 0, i2, AES.CHAR_ENCODING);
        } catch (UnsupportedEncodingException e) {
            Log.w(TAG, "toString() UnsupportedEncodingException");
            return "";
        }
    }

    public static boolean isDebugLogEnable() {
        if (!IS_DEBUG_LOG_ENALBE) {
            return false;
        }
        boolean z = true;
        if (!isDebugBuild() && !IS_HW_LOG) {
            Log.isLoggable(TAG, 3);
            z = false;
        }
        IS_DEBUG_LOG_ENALBE = z;
        return IS_DEBUG_LOG_ENALBE;
    }

    public static boolean isDebugBuild() {
        return IS_DEBUG_LOG_ENALBE;
    }

    private static boolean getHWLog() {
        if (isDebugBuild()) {
            return true;
        }
        try {
            return Log.class.getDeclaredField("HWLog").getBoolean(null);
        } catch (NoSuchFieldException e) {
            Log.e(TAG, "[getHWLog]:  can not find HwLog!");
            return false;
        } catch (IllegalArgumentException e2) {
            Log.e(TAG, "getHWLog is IllegalArgumentException", null);
            return false;
        } catch (IllegalAccessException e3) {
            Log.e(TAG, "getHWLog is IllegalAccessException", null);
            return false;
        } catch (Exception e4) {
            Log.e(TAG, "getHWLog is Exception", null);
            return false;
        }
    }

    public static boolean isHasAgreeLicense() {
        return hasAgreeLicense;
    }

    public static void setHasAgreeLicense(boolean hasAgreeLicense2) {
        hasAgreeLicense = hasAgreeLicense2;
    }

    public static Context getContext() {
        return mContext;
    }

    private static String getTag(String tag) {
        StringBuilder retStr = new StringBuilder(23);
        retStr.append(TAG);
        retStr.append('_');
        if (tag.length() > 16) {
            retStr.append(tag.substring(0, 16));
        } else {
            retStr.append(tag);
        }
        return retStr.toString();
    }

    private static String getLogMsg(String tag, String msg, Throwable e, boolean isNeedProguard) {
        StringBuilder retStr = new StringBuilder(256);
        if (!TextUtils.isEmpty(msg)) {
            if (isDebugBuild()) {
                retStr.append(msg);
            } else if (isNeedProguard) {
                retStr.append(formatLogWithStar(msg));
            } else {
                retStr.append(msg);
            }
        }
        if (e != null) {
            retStr.append(TAB_STR);
            retStr.append(getStackTraceString(e));
        }
        return retStr.toString();
    }

    public static void d(String tag, String msg, boolean isNeedProguard) {
        if (isDebugLogEnable() && !TextUtils.isEmpty(msg)) {
            Log.d(getTag(tag), getLogMsg(tag, msg, null, isNeedProguard));
        }
    }

    public static void d(String tag, String msg, Throwable e, boolean isNeedProguard) {
        if (isDebugLogEnable() && !TextUtils.isEmpty(msg)) {
            Log.d(getTag(tag), getLogMsg(tag, msg, e, isNeedProguard));
        }
    }

    public static void i(String tag, String msg, boolean isNeedProguard) {
        if (!TextUtils.isEmpty(msg)) {
            Log.i(getTag(tag), getLogMsg(tag, msg, null, isNeedProguard));
        }
    }

    public static void i(String tag, String msg, Throwable e, boolean isNeedProguard) {
        if (!TextUtils.isEmpty(msg) || e != null) {
            Log.i(getTag(tag), getLogMsg(tag, msg, e, isNeedProguard));
        }
    }

    public static void e(String tag, String msg, Throwable e, boolean isNeedProguard) {
        if (!TextUtils.isEmpty(msg) || e != null) {
            Log.e(getTag(tag), getLogMsg(tag, msg, e, isNeedProguard), e);
        }
    }

    public static String getStackTraceString(Throwable ex) {
        return Log.getStackTraceString(ex);
    }

    public static String logAddSomeStar(String logString) {
        if (TextUtils.isEmpty(logString) || logString.length() <= 7) {
            return logString;
        }
        StringBuilder temp = new StringBuilder(16);
        temp.append(logString.substring(0, 3));
        temp.append("***");
        temp.append(logString.substring(logString.length() - 3, logString.length()));
        return temp.toString();
    }

    public static String formatLogWithStar(String logStr) {
        if (TextUtils.isEmpty(logStr)) {
            return logStr;
        }
        int len = logStr.length();
        int k = 1;
        if (1 == len) {
            return String.valueOf(STAR);
        }
        StringBuilder retStr = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char charAt = logStr.charAt(i);
            if (mPattern.matcher(String.valueOf(charAt)).matches()) {
                if (k % 2 == 0) {
                    charAt = STAR;
                }
                k++;
            }
            retStr.append(charAt);
        }
        return retStr.toString();
    }

    public static Logger.Builder tag(String tag) {
        return new Logger.Builder().setTag(tag);
    }

    public static Logger.Builder module(String module) {
        return new Logger.Builder().setModule(module);
    }
}
