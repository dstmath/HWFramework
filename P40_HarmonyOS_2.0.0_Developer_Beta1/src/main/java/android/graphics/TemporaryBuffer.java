package android.graphics;

import android.annotation.UnsupportedAppUsage;
import com.android.internal.util.ArrayUtils;

public class TemporaryBuffer {
    private static char[] sTemp = null;

    @UnsupportedAppUsage
    public static char[] obtain(int len) {
        char[] buf;
        synchronized (TemporaryBuffer.class) {
            buf = sTemp;
            sTemp = null;
        }
        if (buf == null || buf.length < len) {
            return ArrayUtils.newUnpaddedCharArray(len);
        }
        return buf;
    }

    @UnsupportedAppUsage
    public static void recycle(char[] temp) {
        if (temp.length <= 1000) {
            synchronized (TemporaryBuffer.class) {
                sTemp = temp;
            }
        }
    }
}
