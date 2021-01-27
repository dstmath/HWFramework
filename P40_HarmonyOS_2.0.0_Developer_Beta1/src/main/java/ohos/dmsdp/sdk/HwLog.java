package ohos.dmsdp.sdk;

import android.util.Log;
import java.lang.reflect.Field;

public class HwLog {
    private static final String TAG = "DMSDPSDK";
    private static boolean isHwDetailLog;
    private static boolean isHwInfo;
    private static boolean isHwModuleDebug;

    static {
        boolean z;
        try {
            Field field = Log.class.getField("HWModuleLog");
            isHwInfo = Log.class.getField("HWINFO").getBoolean(null);
            isHwModuleDebug = field.getBoolean(null);
            if (!isHwInfo) {
                if (!isHwModuleDebug || !Log.isLoggable(TAG, 4)) {
                    z = false;
                    isHwInfo = z;
                    isHwDetailLog = isHwInfo;
                    e(TAG, "isHwDetailLog:" + isHwDetailLog + " HwModuleDebug:" + isHwModuleDebug);
                }
            }
            z = true;
            isHwInfo = z;
            isHwDetailLog = isHwInfo;
            e(TAG, "isHwDetailLog:" + isHwDetailLog + " HwModuleDebug:" + isHwModuleDebug);
        } catch (IllegalArgumentException e) {
            e(TAG, "error:getLogField--IllegalArgumentException" + e.getMessage());
        } catch (IllegalAccessException e2) {
            e(TAG, "error:getLogField--IllegalAccessException" + e2.getMessage());
        } catch (NoSuchFieldException e3) {
            e(TAG, "error:getLogField--NoSuchFieldException" + e3.getMessage());
        }
    }

    public static void e(String str, String str2) {
        Log.e(TAG, str + ":" + str2);
    }

    public static void e(String str, String str2, Throwable th) {
        Log.e(TAG, str + ":" + str2, th);
    }

    public static void w(String str, String str2) {
        Log.w(TAG, str + ":" + str2);
    }

    public static void w(String str, String str2, Throwable th) {
        Log.w(TAG, str + ":" + str2, th);
    }

    public static void i(String str, String str2) {
        Log.i(TAG, str + ":" + str2);
    }

    public static void i(String str, String str2, Throwable th) {
        Log.i(TAG, str + ":" + str2, th);
    }

    public static void d(String str, String str2) {
        if (isHwDetailLog) {
            Log.d(TAG, str + ":" + str2);
        }
    }

    public static void d(String str, String str2, Throwable th) {
        if (isHwDetailLog) {
            Log.d(TAG, str + ":" + str2, th);
        }
    }
}
