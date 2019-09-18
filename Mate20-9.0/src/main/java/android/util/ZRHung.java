package android.util;

public class ZRHung {
    public static final short APPEYE_ANR = 267;
    public static final short APPEYE_BASE = 256;
    public static final short APPEYE_BF = 264;
    public static final short APPEYE_BINDER_TIMEOUT = 281;
    public static final short APPEYE_CANR = 269;
    public static final short APPEYE_CL = 265;
    public static final short APPEYE_CLA = 266;
    public static final short APPEYE_CRASH = 287;
    public static final short APPEYE_FWB_FREEZE = 271;
    public static final short APPEYE_FWB_RECOVER = 288;
    public static final short APPEYE_FWB_WARNING = 270;
    public static final short APPEYE_FWE = 274;
    public static final short APPEYE_MANR = 268;
    public static final short APPEYE_MT = 279;
    public static final short APPEYE_MTO_FREEZE = 261;
    public static final short APPEYE_MTO_INPUT = 263;
    public static final short APPEYE_MTO_SLOW = 262;
    public static final short APPEYE_NFW = 272;
    public static final short APPEYE_NONE = 256;
    public static final short APPEYE_OBS = 277;
    public static final short APPEYE_RCV = 276;
    public static final short APPEYE_RECOVER_RESULT = 278;
    public static final short APPEYE_SBF = 275;
    public static final short APPEYE_SBF_FREEZE = 282;
    public static final short APPEYE_SBF_RECOVER = 283;
    public static final short APPEYE_SUIP_FREEZE = 285;
    public static final short APPEYE_SUIP_INPUT = 286;
    public static final short APPEYE_SUIP_WARNING = 284;
    public static final short APPEYE_TEMP1 = 289;
    public static final short APPEYE_TEMP2 = 290;
    public static final short APPEYE_TEMP3 = 291;
    public static final short APPEYE_TWIN = 273;
    public static final short APPEYE_UIP_FREEZE = 258;
    public static final short APPEYE_UIP_INPUT = 260;
    public static final short APPEYE_UIP_RECOVER = 280;
    public static final short APPEYE_UIP_SLOW = 259;
    public static final short APPEYE_UIP_WARNING = 257;
    public static final int CONFIG_OK = 0;
    public static final int NOT_READY = 1;
    public static final int NOT_SUPPORT = -1;
    public static final int NO_CONFIG = -2;
    public static final String TAG = "ZRHung";
    public static final short XCOLLIE_BASE = 1024;
    public static final short XCOLLIE_FWK_SERVICE = 1025;
    public static final short XCOLLIE_NONE = 1024;
    public static final short ZRHUNG_CAT_APPEYE = 1;
    public static final short ZRHUNG_CAT_EVENT = 2;
    public static final short ZRHUNG_CAT_NUM_MAX = 3;
    public static final short ZRHUNG_CAT_SYS = 0;
    public static final short ZRHUNG_CAT_XCOLLIE = 4;
    public static final short ZRHUNG_EVENT_BACKKEY = 516;
    public static final short ZRHUNG_EVENT_BASE = 512;
    public static final short ZRHUNG_EVENT_HOMEKEY = 515;
    public static final short ZRHUNG_EVENT_LONGPRESS = 513;
    public static final short ZRHUNG_EVENT_MULTIPRESS = 517;
    public static final short ZRHUNG_EVENT_NONE = 512;
    public static final short ZRHUNG_EVENT_POWERKEY = 514;
    public static final short ZRHUNG_EVENT_TEMP1 = 518;
    public static final short ZRHUNG_EVENT_TEMP2 = 519;
    public static final short ZRHUNG_EVENT_TEMP3 = 520;
    public static final short ZRHUNG_WP_APPFREEZE = 23;
    public static final short ZRHUNG_WP_APPFRZ_WARNING = 24;
    public static final short ZRHUNG_WP_BASE = 0;
    public static final short ZRHUNG_WP_CAMERA = 27;
    public static final short ZRHUNG_WP_CPU = 7;
    public static final short ZRHUNG_WP_FDLEAK = 26;
    public static final short ZRHUNG_WP_FENCE = 10;
    public static final short ZRHUNG_WP_FOCUS_WIN = 15;
    public static final short ZRHUNG_WP_GC = 8;
    public static final short ZRHUNG_WP_GPU = 13;
    public static final short ZRHUNG_WP_HTSK_WARNING = 25;
    public static final short ZRHUNG_WP_HUNGTASK = 1;
    public static final short ZRHUNG_WP_INIT = 21;
    public static final short ZRHUNG_WP_IO = 6;
    public static final short ZRHUNG_WP_IPC_OBJECT = 19;
    public static final short ZRHUNG_WP_IPC_THREAD = 18;
    public static final short ZRHUNG_WP_LCD = 16;
    public static final short ZRHUNG_WP_LMKD = 5;
    public static final short ZRHUNG_WP_NONE = 0;
    public static final short ZRHUNG_WP_RES_LEAK = 17;
    public static final short ZRHUNG_WP_SCREENOFF = 3;
    public static final short ZRHUNG_WP_SCREENON = 2;
    public static final short ZRHUNG_WP_SCRNONFWK = 11;
    public static final short ZRHUNG_WP_SERVICE = 12;
    public static final short ZRHUNG_WP_SR = 4;
    public static final short ZRHUNG_WP_TEMP1 = 29;
    public static final short ZRHUNG_WP_TEMP2 = 30;
    public static final short ZRHUNG_WP_TEMP3 = 31;
    public static final short ZRHUNG_WP_TP = 9;
    public static final short ZRHUNG_WP_TRANS_WIN = 14;
    public static final short ZRHUNG_WP_UFS = 20;
    public static final short ZRHUNG_WP_UPLOAD_BIGDATA = 28;
    public static final short ZRHUNG_WP_VMWATCHDOG = 22;

    public static final class HungConfig {
        public final int status;
        public final String value;

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
        if (cmdBuf == null) {
            cmdBuf = "";
        }
        if (Buf == null) {
            Buf = "";
        }
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

    private static String bytesToString(byte[] bs) {
        StringBuilder s = new StringBuilder();
        for (byte b : bs) {
            if (b == 0) {
                break;
            }
            s.append(String.format("%c", new Object[]{Byte.valueOf(b)}));
        }
        return s.toString();
    }
}
