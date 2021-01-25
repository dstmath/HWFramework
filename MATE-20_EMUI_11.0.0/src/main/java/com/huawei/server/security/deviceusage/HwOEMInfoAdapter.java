package com.huawei.server.security.deviceusage;

import android.util.Log;
import com.huawei.util.LogEx;
import java.lang.reflect.InvocationTargetException;

public class HwOEMInfoAdapter {
    private static final String CHR_ISBN_READ_CLASS = "com.huawei.android.os.HwDeviceInfoCustEx";
    private static final String CHR_OEM_INFO_CLASS = "com.huawei.android.os.HwOemInfoCustEx";
    private static final boolean IS_HW_DEBUG = (LogEx.getLogHWInfo() || (LogEx.getHWModuleLog() && Log.isLoggable(TAG, 4)));
    private static final String TAG = "HwOEMInfoAdapter";

    private HwOEMInfoAdapter() {
    }

    public static byte[] getByteArrayFromOeminfo(int type, int size) {
        if (IS_HW_DEBUG) {
            Log.i(TAG, "getByteArrayFromOeminfo has run");
        }
        byte[] byteArray = new byte[0];
        try {
            Object obj = Class.forName(CHR_OEM_INFO_CLASS).getMethod("getByteArrayFromOeminfo", Integer.TYPE, Integer.TYPE).invoke(null, Integer.valueOf(type), Integer.valueOf(size));
            if (obj == null || !(obj instanceof byte[])) {
                return byteArray;
            }
            return (byte[]) obj;
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "getByteArrayFromOeminfo unable to find class!");
            return byteArray;
        } catch (NoSuchMethodException e2) {
            Log.e(TAG, "getByteArrayFromOeminfo method not found!");
            return byteArray;
        } catch (IllegalAccessException e3) {
            Log.e(TAG, "getByteArrayFromOeminfo IllegalAccessException!");
            return byteArray;
        } catch (InvocationTargetException e4) {
            Log.e(TAG, "getByteArrayFromOeminfo InvocationTargetException!");
            return byteArray;
        }
    }

    public static int writeByteArrayToOeminfo(int type, int size, byte[] bytes) {
        if (IS_HW_DEBUG) {
            Log.i(TAG, "writeByteArrayToOeminfo has run");
        }
        try {
            Object obj = Class.forName(CHR_OEM_INFO_CLASS).getMethod("writeByteArrayToOeminfo", Integer.TYPE, Integer.TYPE, byte[].class).invoke(null, Integer.valueOf(type), Integer.valueOf(size), bytes);
            if (obj == null || !(obj instanceof Integer)) {
                return 0;
            }
            return ((Integer) obj).intValue();
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Unable to find class!");
            return 0;
        } catch (NoSuchMethodException e2) {
            Log.e(TAG, "writeByteArrayToOeminfo method not found!");
            return 0;
        } catch (IllegalAccessException e3) {
            Log.e(TAG, "writeByteArrayToOeminfo IllegalAccessException!");
            return 0;
        } catch (InvocationTargetException e4) {
            Log.e(TAG, "writeByteArrayToOeminfo InvocationTargetException!");
            return 0;
        }
    }

    public static String getIsbnOrSn(int id) {
        if (IS_HW_DEBUG) {
            Log.i(TAG, "getIsbnOrSn has run");
        }
        try {
            Object obj = Class.forName(CHR_ISBN_READ_CLASS).getMethod("getISBNOrSN", Integer.TYPE).invoke(null, Integer.valueOf(id));
            if (obj == null || !(obj instanceof String)) {
                return null;
            }
            return (String) obj;
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "getIsbnOrSn unable to find class!");
            return null;
        } catch (NoSuchMethodException e2) {
            Log.e(TAG, "getIsbnOrSn method not found!");
            return null;
        } catch (IllegalAccessException e3) {
            Log.e(TAG, "IllegalAccessException has been thrown while trying to invoke getIsbnOrSn");
            return null;
        } catch (InvocationTargetException e4) {
            Log.e(TAG, "InvocationTargetException has been thrown while trying to invoke getIsbnOrSn");
            return null;
        }
    }
}
