package com.huawei.dmsdpsdk2.util;

import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.android.hwpartdevicevirtualization.BuildConfig;

public class Util {
    private static final int BYTE_MASK = 255;
    private static final int EXPECTED_BUFFER_DATA = 1024;
    private static final int INT_EIGHT = 8;
    private static final int INT_FOUR = 4;
    private static final int INT_SIXTEEN = 16;
    private static final int INT_TEN = 10;
    private static final int INT_TWENTY_FOUR = 24;
    private static final int INT_TWO = 2;
    private static final int MAX_DATA = 128;
    private static final String TAG = "Util";

    private Util() {
    }

    public static void arraycopy(Object src, int srcPos, Object dest, int destPos, int length) {
        System.arraycopy(src, srcPos, dest, destPos, length);
    }

    public static byte[] intToByteArray(int para) {
        return new byte[]{(byte) ((para >> 24) & 255), (byte) ((para >> 16) & 255), (byte) ((para >> 8) & 255), (byte) (para & 255)};
    }

    public static int byteArrayToInt(byte[] bytes) {
        return (bytes[3] & 255) | ((bytes[2] & 255) << 8) | ((bytes[1] & 255) << 16) | ((bytes[0] & 255) << 24);
    }

    public static byte[] parcelableToByteArray(Parcelable p) {
        if (p == null) {
            return new byte[0];
        }
        Parcel parcel = Parcel.obtain();
        p.writeToParcel(parcel, 0);
        byte[] bytes = parcel.marshall();
        parcel.recycle();
        return bytes;
    }

    public static <T> T byteArrayToParcelable(byte[] b, Parcelable.Creator<T> creator) {
        if (b == null || b.length == 0) {
            return null;
        }
        Parcel pa = Parcel.obtain();
        pa.unmarshall(b, 0, b.length);
        pa.setDataPosition(0);
        T parcel = creator.createFromParcel(pa);
        pa.recycle();
        return parcel;
    }

    public static String anonymizeData(String info) {
        return anonymizeData(info, 2, 2, true);
    }

    public static String anonymizeData(String info, int left, int right, boolean basedOnLeft) {
        if (info == null || info.isEmpty()) {
            return BuildConfig.FLAVOR;
        }
        StringBuilder sbText = new StringBuilder(1024);
        int length = info.length();
        if ((length - left) - right > 0) {
            String prefix = info.substring(0, left);
            String suffix = info.substring(length - right);
            if (prefix.length() < 128) {
                sbText.append(prefix);
            }
            sbText.append("****");
            if (suffix.length() < 128) {
                sbText.append(suffix);
            }
        } else if (basedOnLeft) {
            if (length <= left || left <= 0) {
                sbText.append(info.substring(0, 1));
                sbText.append("****");
            } else {
                sbText.append(info.substring(0, left));
                sbText.append("****");
            }
        } else if (length <= right || right <= 0) {
            sbText.append("****");
            sbText.append(info.substring(info.length() - 1));
        } else {
            sbText.append("****");
            sbText.append(info.substring(length - right));
        }
        return sbText.toString();
    }
}
