package ohos.dmsdp.sdk.util;

import android.os.Parcel;
import android.os.Parcelable;

public class Util {
    private static final int BYTE_MASK = 255;
    private static final int INT_EIGHT = 8;
    private static final int INT_FOUR = 4;
    private static final int INT_SIXTEEN = 16;
    private static final int INT_TEN = 10;
    private static final int INT_TWENTY_FOUR = 24;
    private static final int INT_TWO = 2;
    private static final String TAG = "Util";

    public static byte[] intToByteArray(int i) {
        return new byte[]{(byte) ((i >> 24) & 255), (byte) ((i >> 16) & 255), (byte) ((i >> 8) & 255), (byte) (i & 255)};
    }

    private Util() {
    }

    public static void arraycopy(Object obj, int i, Object obj2, int i2, int i3) {
        System.arraycopy(obj, i, obj2, i2, i3);
    }

    public static int byteArrayToInt(byte[] bArr) {
        return ((bArr[0] & 255) << 24) | (bArr[3] & 255) | ((bArr[2] & 255) << 8) | ((bArr[1] & 255) << 16);
    }

    public static byte[] parcelableToByteArray(Parcelable parcelable) {
        if (parcelable == null) {
            return new byte[0];
        }
        Parcel obtain = Parcel.obtain();
        parcelable.writeToParcel(obtain, 0);
        byte[] marshall = obtain.marshall();
        obtain.recycle();
        return marshall;
    }

    public static <T> T byteArrayToParcelable(byte[] bArr, Parcelable.Creator<T> creator) {
        if (bArr == null || bArr.length == 0) {
            return null;
        }
        Parcel obtain = Parcel.obtain();
        obtain.unmarshall(bArr, 0, bArr.length);
        obtain.setDataPosition(0);
        T createFromParcel = creator.createFromParcel(obtain);
        obtain.recycle();
        return createFromParcel;
    }
}
