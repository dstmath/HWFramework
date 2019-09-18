package com.huawei.android.os;

public class HwOemInfoCustEx {
    public static final int firstTimeLength = 16;
    public static final int firstTimeType = 37;

    public static final native boolean native_bTAddressIsNull();

    public static final native byte[] native_getByteArrayFromOeminfo(int i, int i2);

    private static final native int native_getFastbootStatus();

    public static final native String native_getStringFromOeminfo(int i, int i2);

    public static final native int native_nffwriteImeiToOeminfo(String str);

    public static final native int native_writeByteArrayToOeminfo(int i, int i2, byte[] bArr);

    public static final native int native_writeStringToOeminfo(int i, int i2, String str);

    static {
        System.loadLibrary("oeminfo_jni");
    }

    public static final int nffwriteImeiToOeminfo(String imeiStr) {
        return native_nffwriteImeiToOeminfo(imeiStr);
    }

    public static final boolean bTAddressIsNull() {
        return native_bTAddressIsNull();
    }

    public static final int writeStringToOeminfo(int type, int sizeOf, String mStr) {
        return native_writeStringToOeminfo(type, sizeOf, mStr);
    }

    public static final int writeByteArrayToOeminfo(int type, int sizeOf, byte[] mByteArray) {
        return native_writeByteArrayToOeminfo(type, sizeOf, mByteArray);
    }

    public static final String getStringFromOeminfo(int type, int sizeOf) {
        return native_getStringFromOeminfo(type, sizeOf);
    }

    public static final byte[] getByteArrayFromOeminfo(int type, int sizeOf) {
        return native_getByteArrayFromOeminfo(type, sizeOf);
    }

    public static final int writeFirstStartTime(String mStr) {
        return native_writeStringToOeminfo(37, 16, mStr);
    }

    public static final String getFirstStartTime() {
        return native_getStringFromOeminfo(37, 16);
    }

    public static final int getFastbootStatus() {
        return native_getFastbootStatus();
    }
}
