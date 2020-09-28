package android.rms.utils;

import android.app.AppGlobals;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;
import com.huawei.uikit.effect.BuildConfig;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class Utils {
    public static final String BUNDLE_CURRENT_COUNT = "current_count";
    public static final String BUNDLE_HARD_THRESHOLD = "hard_threshold";
    public static final String BUNDLE_IS_IN_WHITELIST = "isInWhiteList";
    public static final String BUNDLE_THIRD_PARTY_APP_LIFETIME = "third_party_app_lifetime";
    public static final String BUNDLE_THIRD_PARTY_APP_USETIME = "third_party_app_usetime";
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATE_FORMAT_DETAIL = "yyyy-MM-dd hh:mm:ss";
    public static final long DATE_TIME_24HOURS = 86400000;
    public static final boolean DEBUG = (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3)));
    public static final int FLAG_CRASH_MONITOR = 2;
    public static final int FLAG_IO_STATISTIC = 1;
    public static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    public static final boolean HWLOGW_E = true;
    public static final boolean IS_DEBUG_VERSION;
    private static final String PARAM_SPLIT = ":";
    public static final int RMSVERSION = SystemProperties.getInt("ro.config.RmsVersion", 2);
    public static final int SELF_COUNT_RESOURCE = -1;
    public static final String TAG = "RMS";

    static {
        boolean z = false;
        if (SystemProperties.getInt("ro.logsystem.usertype", 1) == 3) {
            z = true;
        }
        IS_DEBUG_VERSION = z;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001a, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:?, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x001f, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0020, code lost:
        r2.addSuppressed(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0023, code lost:
        throw r3;
     */
    public static boolean writeFile(String path, String data) {
        if (path == null || data == null) {
            return false;
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(path);
            fileOutputStream.write(data.getBytes("UTF-8"));
            fileOutputStream.close();
            return true;
        } catch (IOException e) {
            Log.w(TAG, "Unable to write file exception");
            return false;
        }
    }

    public static boolean generateDirectory(String path) {
        if (path == null) {
            return false;
        }
        try {
            File directory = new File(path);
            if (!directory.exists()) {
                return directory.mkdirs();
            }
            return false;
        } catch (SecurityException e) {
            Log.w(TAG, "mkdir fail");
            return false;
        }
    }

    public static void wait(int ms) {
        try {
            Thread.sleep((long) ms);
        } catch (InterruptedException e) {
            Log.w(TAG, "InterruptedException");
        }
    }

    public static String getPackageNameByUid(int uid) {
        try {
            if (AppGlobals.getPackageManager() != null) {
                return AppGlobals.getPackageManager().getNameForUid(uid);
            }
            return BuildConfig.FLAVOR;
        } catch (RemoteException e) {
            Log.e(TAG, "get pkg name fail");
            return BuildConfig.FLAVOR;
        }
    }

    public static boolean scanArgs(String[] args, String value) {
        if (!(args == null || value == null)) {
            for (String arg : args) {
                if (value.equals(arg)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String scanArgsWithParam(String[] args, String key) {
        String result = null;
        if (args == null || key == null) {
            Log.e(TAG, "scanArgsWithParam,neither args or key is null");
            return null;
        }
        for (String arg : args) {
            if (arg != null && arg.contains(key)) {
                String[] splitsArray = arg.split(PARAM_SPLIT);
                if (splitsArray.length < 2) {
                    break;
                }
                result = splitsArray[1];
            }
        }
        return result;
    }

    public static String getDateFormatValue(long time) {
        return new SimpleDateFormat(DATE_FORMAT, Locale.US).format(new Date(time));
    }

    public static String getDateFormatValue(long time, String format) {
        if (format == null) {
            return null;
        }
        return new SimpleDateFormat(format).format(new Date(time));
    }

    public static long getShortDateFormatValue(long time) {
        SimpleDateFormat sdFormatter = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        try {
            return sdFormatter.parse(sdFormatter.format(new Date(time))).getTime();
        } catch (ParseException e) {
            Log.e(TAG, "getShortDateFormatValue");
            return 0;
        }
    }

    public static long getDifferencesByDay(long time1, long time2) {
        return (time1 - time2) / 86400000;
    }

    public static long getSizeOfDirectory(File directory) {
        long totalSizeInDirectory = 0;
        if (directory == null) {
            return 0;
        }
        try {
            if (!directory.exists()) {
                Log.e(TAG, "getSizeOfDirectory," + directory.getCanonicalPath() + " not exists");
                return 0;
            }
            String[] subFiles = directory.list();
            if (subFiles != null) {
                for (String name : subFiles) {
                    totalSizeInDirectory += new File(directory, name).length();
                }
            }
            return totalSizeInDirectory;
        } catch (IOException ex) {
            Log.e(TAG, "getSizeOfDirectory,IOException occurs:" + ex.getMessage());
        }
    }

    public static int getCompactPeriodInterval() {
        int compactPeriodInterval = SystemProperties.getInt("rms_debug_interval", 300000);
        Log.w(TAG, "Debug CompactPeriodInterval is : " + compactPeriodInterval);
        return compactPeriodInterval;
    }
}
