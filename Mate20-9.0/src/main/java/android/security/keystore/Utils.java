package android.security.keystore;

import java.util.Date;

abstract class Utils {
    private Utils() {
    }

    static Date cloneIfNotNull(Date value) {
        if (value != null) {
            return (Date) value.clone();
        }
        return null;
    }

    static byte[] cloneIfNotNull(byte[] value) {
        if (value != null) {
            return (byte[]) value.clone();
        }
        return null;
    }
}
