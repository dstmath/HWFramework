package com.android.server.security.deviceusage;

import android.util.Slog;
import java.lang.reflect.InvocationTargetException;

public class HwOEMInfoAdapter {
    public static final String CHR_ISBNREAD_CLASS = "com.huawei.android.os.HwDeviceInfoCustEx";
    public static final String CHR_OEMINFO_CLASS = "com.huawei.android.os.HwOemInfoCustEx";
    private static final String TAG = "HwOEMInfoAdapter";
    private static Class chrClass = null;

    public static byte[] getByteArrayFromOeminfo(int type, int sizeOf) {
        Slog.e(TAG, "getByteArrayFromOeminfo has run ");
        try {
            chrClass = Class.forName(CHR_OEMINFO_CLASS);
            return (byte[]) chrClass.getMethod("getByteArrayFromOeminfo", new Class[]{Integer.TYPE, Integer.TYPE}).invoke(null, new Object[]{Integer.valueOf(type), Integer.valueOf(sizeOf)});
        } catch (ClassNotFoundException e) {
            Slog.e(TAG, "Unable to find class com.huawei.android.os.HwOemInfoCustEx");
            return new byte[0];
        } catch (NoSuchMethodException e2) {
            Slog.e(TAG, "getByteArrayFromOeminfo method not found in class com.huawei.android.os.HwOemInfoCustEx");
            return new byte[0];
        } catch (IllegalAccessException e3) {
            Slog.e(TAG, "IllegalAccessException has been thrown while trying to invode getByteArrayFromOeminfo");
            return new byte[0];
        } catch (InvocationTargetException e4) {
            Slog.e(TAG, "InvocationTargetException has been thrown while trying to invode getByteArrayFromOeminfo");
            return new byte[0];
        }
    }

    public static int writeByteArrayToOeminfo(int type, int sizeOf, byte[] mByte) {
        Slog.e(TAG, "writeByteArrayToOeminfo has run ");
        try {
            chrClass = Class.forName(CHR_OEMINFO_CLASS);
            return ((Integer) chrClass.getMethod("writeByteArrayToOeminfo", new Class[]{Integer.TYPE, Integer.TYPE, byte[].class}).invoke(null, new Object[]{Integer.valueOf(type), Integer.valueOf(sizeOf), mByte})).intValue();
        } catch (ClassNotFoundException e) {
            Slog.e(TAG, "Unable to find class com.huawei.android.os.HwOemInfoCustEx");
            return 0;
        } catch (NoSuchMethodException e2) {
            Slog.e(TAG, "writeByteArrayToOeminfo method not found in class com.huawei.android.os.HwOemInfoCustEx");
            return 0;
        } catch (IllegalAccessException e3) {
            Slog.e(TAG, "IllegalAccessException has been thrown while trying to invode writeByteArrayToOeminfo");
            return 0;
        } catch (InvocationTargetException e4) {
            Slog.e(TAG, "InvocationTargetException has been thrown while trying to invode writeByteArrayToOeminfo");
            return 0;
        }
    }

    public static String getISBNOrSN(int id) {
        Slog.e(TAG, "getISBNOrSN has run ");
        try {
            chrClass = Class.forName(CHR_ISBNREAD_CLASS);
            return (String) chrClass.getMethod("getISBNOrSN", new Class[]{Integer.TYPE}).invoke(null, new Object[]{Integer.valueOf(id)});
        } catch (ClassNotFoundException e) {
            Slog.e(TAG, "Unable to find class com.huawei.android.os.HwDeviceInfoCustEx");
            return null;
        } catch (NoSuchMethodException e2) {
            Slog.e(TAG, "getISBNOrSN method not found in class com.huawei.android.os.HwOemInfoCustEx");
            return null;
        } catch (IllegalAccessException e3) {
            Slog.e(TAG, "IllegalAccessException has been thrown while trying to invode getISBNOrSN");
            return null;
        } catch (InvocationTargetException e4) {
            Slog.e(TAG, "InvocationTargetException has been thrown while trying to invode getISBNOrSN");
            return null;
        }
    }
}
