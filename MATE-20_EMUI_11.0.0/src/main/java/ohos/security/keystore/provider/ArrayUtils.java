package ohos.security.keystore.provider;

public class ArrayUtils {
    private ArrayUtils() {
    }

    public static byte[] cloneIfNotEmpty(byte[] bArr) {
        return (bArr == null || bArr.length <= 0) ? bArr : (byte[]) bArr.clone();
    }

    public static byte[] subarray(byte[] bArr, int i, int i2) {
        if (i2 == 0 || bArr == null) {
            return new byte[0];
        }
        if (i < 0 || i >= bArr.length || i2 < 0 || i2 >= bArr.length || bArr.length - i < i2) {
            return bArr;
        }
        byte[] bArr2 = new byte[i2];
        System.arraycopy(bArr, i, bArr2, 0, i2);
        return bArr2;
    }
}
