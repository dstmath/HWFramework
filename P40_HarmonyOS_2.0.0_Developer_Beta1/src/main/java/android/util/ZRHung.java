package android.util;

import com.huawei.hwpartdfr.BuildConfig;
import java.util.Locale;

public class ZRHung {
    public static final short APPEYE_RECOVER_RESULT = 278;
    private static final int BYTES_ARRAY_SIZE_LIMIT = 512;
    public static final int CONFIG_OK = 0;
    public static final String TAG = "ZRHung";
    public static final short ZRHUNG_WP_FOCUS_WIN = 15;
    public static final short ZRHUNG_WP_TRANS_WIN = 14;

    public static final class HungConfig {
        public final int status;
        public final String value;

        private HungConfig() {
            this(-1, null);
        }

        private HungConfig(int status2, String value2) {
            this.status = status2;
            this.value = value2;
        }
    }

    public static boolean sendHungEvent(short wp, String cmdBuf, String buf) {
        String str = BuildConfig.FLAVOR;
        String str2 = cmdBuf == null ? str : cmdBuf;
        if (buf != null) {
            str = buf;
        }
        try {
            return IMonitorNative.sendHungEvent(wp, str2, str);
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "sendHungEvent failed for no implementation of native");
            return false;
        }
    }

    public static HungConfig getHungConfig(short wp) {
        try {
            byte[] data = new byte[BYTES_ARRAY_SIZE_LIMIT];
            int status = IMonitorNative.getHungConfig(wp, data);
            if (status < 0) {
                return null;
            }
            return new HungConfig(status, bytesToString(data));
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "getHungConfig failed for no implementation of native");
            return null;
        }
    }

    private static String bytesToString(byte[] bytes) {
        StringBuilder sb = new StringBuilder((int) BYTES_ARRAY_SIZE_LIMIT);
        for (byte tmpByte : bytes) {
            if (tmpByte == 0) {
                break;
            }
            sb.append(String.format(Locale.ENGLISH, "%c", Byte.valueOf(tmpByte)));
        }
        return sb.toString();
    }
}
