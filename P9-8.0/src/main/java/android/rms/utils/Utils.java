package android.rms.utils;

import android.app.AppGlobals;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
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
    public static final long DATE_TIME_24HOURS = 86400000;
    public static final boolean DEBUG;
    public static final int FLAG_CRASH_MONITOR = 2;
    public static final int FLAG_IO_STATISTIC = 1;
    public static final boolean HWFLOW;
    public static final boolean HWLOGW_E = true;
    public static final boolean IS_DEBUG_VERSION;
    private static final String PARAM_SPLIT = ":";
    public static final int RMSVERSION = SystemProperties.getInt("ro.config.RmsVersion", 2);
    public static final int SELF_COUNT_RESOURCE = -1;
    public static final String TAG = "RMS";

    static {
        boolean z = true;
        boolean isLoggable = !Log.HWLog ? Log.HWModuleLog ? Log.isLoggable(TAG, 3) : false : true;
        DEBUG = isLoggable;
        if (Log.HWINFO) {
            isLoggable = true;
        } else if (Log.HWModuleLog) {
            isLoggable = Log.isLoggable(TAG, 4);
        } else {
            isLoggable = false;
        }
        HWFLOW = isLoggable;
        if (SystemProperties.getInt("ro.logsystem.usertype", 1) != 3) {
            z = false;
        }
        IS_DEBUG_VERSION = z;
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0050 A:{SYNTHETIC, Splitter: B:17:0x0050} */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0062 A:{SYNTHETIC, Splitter: B:23:0x0062} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static final boolean writeFile(String path, String data) {
        IOException e;
        Throwable th;
        FileOutputStream fos = null;
        boolean success = true;
        try {
            FileOutputStream fos2 = new FileOutputStream(path);
            try {
                fos2.write(data.getBytes("UTF-8"));
                if (fos2 != null) {
                    try {
                        fos2.close();
                    } catch (IOException e2) {
                        Log.w(TAG, "writeFile : IOException when close");
                    }
                }
                fos = fos2;
            } catch (IOException e3) {
                e = e3;
                fos = fos2;
                try {
                    Log.w(TAG, "Unable to write " + path + " msg=" + e.getMessage());
                    success = false;
                    if (fos != null) {
                    }
                    return success;
                } catch (Throwable th2) {
                    th = th2;
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e4) {
                            Log.w(TAG, "writeFile : IOException when close");
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fos = fos2;
                if (fos != null) {
                }
                throw th;
            }
        } catch (IOException e5) {
            e = e5;
            Log.w(TAG, "Unable to write " + path + " msg=" + e.getMessage());
            success = false;
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e6) {
                    Log.w(TAG, "writeFile : IOException when close");
                }
            }
            return success;
        }
        return success;
    }

    public static final boolean generateDirectory(String path) {
        boolean flag = false;
        if (path == null) {
            return false;
        }
        try {
            File directory = new File(path);
            if (!directory.exists()) {
                flag = directory.mkdirs();
            }
        } catch (SecurityException e) {
            Log.w(TAG, "mkdir fail");
        }
        return flag;
    }

    public static final void wait(int ms) {
        try {
            Thread.sleep((long) ms);
        } catch (InterruptedException e) {
        }
    }

    public static final String getPackageNameByUid(int uid) {
        String pkgName = "";
        try {
            return AppGlobals.getPackageManager().getNameForUid(uid);
        } catch (RemoteException e) {
            Log.e(TAG, "get pkg name fail");
            return pkgName;
        }
    }

    public static boolean scanArgs(String[] args, String value) {
        if (args != null) {
            for (String arg : args) {
                if (value.equals(arg)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String scanArgsWithParam(String[] args, String key) {
        if (args == null || key == null) {
            Log.e(TAG, "scanArgsWithParam,neither args or key is null");
            return null;
        }
        String result = null;
        for (String arg : args) {
            if (arg != null && (arg.contains(key) ^ 1) == 0) {
                String[] splitsArray = arg.split(PARAM_SPLIT);
                if (splitsArray.length < 2) {
                    break;
                }
                result = splitsArray[1];
            }
        }
        return result;
    }

    public static Object invokeMethod(Object instance, String methodName, Class[] parameterType, Object... argsValues) {
        if (instance == null) {
            Log.e(TAG, "invokeMethod,instance is null");
            return null;
        }
        Object resultObj = null;
        try {
            Method method;
            Class<?> classObj = instance.getClass();
            if (parameterType != null) {
                method = classObj.getDeclaredMethod(methodName, parameterType);
            } else {
                method = classObj.getDeclaredMethod(methodName, new Class[0]);
            }
            final Method methodResult = method;
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Void run() {
                    methodResult.setAccessible(true);
                    return null;
                }
            });
            resultObj = method.invoke(instance, argsValues);
        } catch (RuntimeException e) {
            Log.e(TAG, "invokeMethod,RuntimeException method:" + methodName + ",msg:" + e.getMessage());
        } catch (NoSuchMethodException e2) {
            Log.e(TAG, "invokeMethod,no such method:" + methodName + ",msg:" + e2.getMessage());
        } catch (IllegalAccessException e3) {
            Log.e(TAG, "invokeMethod,IllegalAccessException,method:" + methodName + ",msg:" + e3.getMessage());
        } catch (InvocationTargetException ex) {
            Log.e(TAG, "invokeMethod,Exception,method:" + methodName + ",msg:" + ex.getMessage());
        }
        return resultObj;
    }

    public static String getDateFormatValue(long time) {
        return new SimpleDateFormat(DATE_FORMAT, Locale.US).format(new Date(time));
    }

    public static long getShortDateFormatValue(long time) {
        SimpleDateFormat sdFormatter = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        try {
            return sdFormatter.parse(sdFormatter.format(new Date(time))).getTime();
        } catch (ParseException e) {
            Log.e(TAG, "getShortDateFormatValue:" + e.getMessage());
            return 0;
        }
    }

    public static long getDifferencesByDay(long time1, long time2) {
        return (time1 - time2) / DATE_TIME_24HOURS;
    }

    public static long getSizeOfDirectory(File directory) {
        long totalSizeInDirectory = 0;
        try {
            if (directory.exists()) {
                String[] subFiles = directory.list();
                if (subFiles != null) {
                    for (String name : subFiles) {
                        totalSizeInDirectory += new File(directory, name).length();
                    }
                }
                return totalSizeInDirectory;
            }
            Log.e(TAG, "getSizeOfDirectory," + directory.getCanonicalPath() + " not exists");
            return 0;
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
