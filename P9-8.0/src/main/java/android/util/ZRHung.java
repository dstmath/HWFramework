package android.util;

public class ZRHung {
    public static int CONFIG_OK = 0;
    public static int NOT_READY = 1;
    public static int NOT_SUPPORT = -1;
    public static int NO_CONFIG = -2;
    public static final String TAG = "ZRHung";
    public static final short ZRHUNG_WP_CPU = (short) 7;
    public static final short ZRHUNG_WP_DARK_WIN = (short) 16;
    public static final short ZRHUNG_WP_FENCE = (short) 10;
    public static final short ZRHUNG_WP_FOCUS_WIN = (short) 15;
    public static final short ZRHUNG_WP_GC = (short) 8;
    public static final short ZRHUNG_WP_GPU = (short) 13;
    public static final short ZRHUNG_WP_HUNGTASK = (short) 1;
    public static final short ZRHUNG_WP_IO = (short) 6;
    public static final short ZRHUNG_WP_IPC_OBJECT = (short) 19;
    public static final short ZRHUNG_WP_IPC_THREAD = (short) 18;
    public static final short ZRHUNG_WP_LMKD = (short) 5;
    public static final short ZRHUNG_WP_RES_LEAK = (short) 17;
    public static final short ZRHUNG_WP_SCREENOFF = (short) 3;
    public static final short ZRHUNG_WP_SCREENON = (short) 2;
    public static final short ZRHUNG_WP_SERVICE = (short) 12;
    public static final short ZRHUNG_WP_SF = (short) 11;
    public static final short ZRHUNG_WP_SR = (short) 4;
    public static final short ZRHUNG_WP_TP = (short) 9;
    public static final short ZRHUNG_WP_TRANS_WIN = (short) 14;

    public static final class HungConfig {
        public final int status;
        public final String value;

        /* synthetic */ HungConfig(int s, String val, HungConfig -this2) {
            this(s, val);
        }

        private HungConfig() {
            this.status = -1;
            this.value = null;
        }

        private HungConfig(int s, String val) {
            this.status = s;
            this.value = val;
        }
    }

    public static boolean sendHungEvent(short wp, String cmdBuf, String Buf) {
        try {
            return IMonitorNative.sendHungEvent(wp, cmdBuf, Buf);
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "sendHungEvent failed for no implementation of native");
            return false;
        }
    }

    public static HungConfig getHungConfig(short wp) {
        try {
            byte[] data = new byte[512];
            return new HungConfig(IMonitorNative.getHungConfig(wp, data), bytesToString(data), null);
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "getHungConfig failed for no implementation of native");
            return null;
        }
    }

    private static String bytesToString(byte[] bs) {
        StringBuilder s = new StringBuilder();
        for (byte b : bs) {
            if (b == (byte) 0) {
                Log.w(TAG, "should end this string copy!");
                break;
            }
            s.append(String.format("%c", new Object[]{Byte.valueOf(bs[r2])}));
        }
        return s.toString();
    }
}
