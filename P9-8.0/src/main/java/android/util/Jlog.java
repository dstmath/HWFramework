package android.util;

import android.os.Environment;
import android.os.IBinder;
import android.os.SystemProperties;
import com.android.internal.telephony.cdma.HwCustPlusAndIddNddConvertUtils;
import java.io.File;

public final class Jlog {
    private static final String FRAMETEST_FLAG_FILE = "etc/frametest.xml";
    public static final int HW_LOG_ID_JANK = 6;
    public static final long INFLATE_TIME_LIMIT_NS = 16000000;
    public static final int JANK_UPDATE_PROP_DELAY = 900000;
    public static final int JANK_UPDATE_PROP_INTERVAL = 30000;
    public static final long MAX_VIOLATION_DURATION_MS = 8000;
    public static final long MIN_VIOLATION_DURATION_MS = 320;
    public static final long OBTAINVIEW_TIME_LIMIT_NS = 16000000;
    public static final int OPENGL_SKIPPED_FRAME_LIMIT = 30;
    private static final String PERFTEST_FLAG_FILE = "etc/perftest.xml";
    public static final int PERF_CTRL_TYPE_HIGHPERF = 0;
    public static final int PERF_CTRL_TYPE_LOW_VOLTAGE = 3;
    public static final int PERF_CTRL_TYPE_MAX = 4;
    public static final int PERF_CTRL_TYPE_SPEC_SCENE = 1;
    public static final int PERF_CTRL_TYPE_THERMAL_PROTECT = 2;
    public static final int PERF_EVENT_ANIMATION = 7;
    public static final int PERF_EVENT_APP_START = 4;
    public static final int PERF_EVENT_BOOT_COMPLETE = 0;
    public static final int PERF_EVENT_INTERACTION = 1;
    public static final int PERF_EVENT_LIST_FLING = 8;
    public static final int PERF_EVENT_LUCKYMONEY = 12;
    public static final int PERF_EVENT_MAX = 13;
    public static final int PERF_EVENT_OFF = 0;
    public static final int PERF_EVENT_ON = 1;
    public static final int PERF_EVENT_PROBE = 4097;
    public static final int PERF_EVENT_RAW_REQ = 4096;
    public static final int PERF_EVENT_ROTATING = 6;
    public static final int PERF_EVENT_SCREEN_OFF = 10;
    public static final int PERF_EVENT_SCREEN_ON = 9;
    public static final int PERF_EVENT_STATUSBAR = 11;
    public static final int PERF_EVENT_VSYNC_OFF = 3;
    public static final int PERF_EVENT_VSYNC_ON = 2;
    public static final int PERF_EVENT_WINDOW_SWITCH = 5;
    public static final int PERF_HMP_POLICY_STATE_OFF = 0;
    public static final int PERF_HMP_POLICY_STATE_ON = 1;
    public static final int PERF_HMP_PRIORITY_0 = 0;
    public static final int PERF_HMP_PRIORITY_1 = 1;
    public static final int PERF_HMP_PRIORITY_2 = 2;
    public static final int PERF_HMP_PRIORITY_3 = 3;
    public static final int PERF_HMP_PRIORITY_4 = 4;
    public static final int PERF_HMP_PRIORITY_5 = 5;
    public static final int PERF_HMP_PRIORITY_MAX = 6;
    public static final int PERF_TAG_B_CPU_CUR = 6;
    public static final int PERF_TAG_B_CPU_MAX = 5;
    public static final int PERF_TAG_B_CPU_MIN = 4;
    public static final int PERF_TAG_CTRL_TYPE = 0;
    public static final int PERF_TAG_DDR_CUR = 12;
    public static final int PERF_TAG_DDR_MAX = 11;
    public static final int PERF_TAG_DDR_MIN = 10;
    public static final int PERF_TAG_GPU_CUR = 9;
    public static final int PERF_TAG_GPU_MAX = 8;
    public static final int PERF_TAG_GPU_MIN = 7;
    public static final int PERF_TAG_HMP_DN_THRES = 14;
    public static final int PERF_TAG_HMP_POLICY_STATE = 16;
    public static final int PERF_TAG_HMP_PRIORITY = 15;
    public static final int PERF_TAG_HMP_UP_THRES = 13;
    public static final int PERF_TAG_IPA_CONTROL_TEMP = 18;
    public static final int PERF_TAG_IPA_SUSTAINABLE_POWER = 19;
    public static final int PERF_TAG_IPA_SWITCH_TEMP = 17;
    public static final int PERF_TAG_L_CPU_CUR = 3;
    public static final int PERF_TAG_L_CPU_MAX = 2;
    public static final int PERF_TAG_L_CPU_MIN = 1;
    public static final int PERF_TAG_MAX = 20;
    public static final int REQUEST_FAILED = -1;
    public static final int REQUEST_SUCCEEDED = 0;
    public static final long SETUPVIEW_TIME_LIMIT_NS = 16000000;
    public static final int SKIPPED_FRAME_LIMIT = 5;
    private static IBinder mService = null;
    private static final boolean mbBetaUser;
    private static boolean mbEmuiAnimation = false;
    private static boolean mbFrameTestEnabled;
    private static boolean mbPerfTestEnabled;
    private static final boolean mbUBMEnable = SystemProperties.getBoolean("ro.config.hw_ubmenable", false);
    private static int misPerfhubEnable = -1;
    private static int mnEmuiAnimationSkipFrames = 0;
    private static String msEmuiAnimationName;
    private static StringBuilder msEmuiAnimationSkippedFrames = new StringBuilder(32);
    private static String msLaunchingPkgName;

    public static native int print_janklog_native(int i, int i2, String str);

    static {
        boolean z;
        mbPerfTestEnabled = false;
        mbFrameTestEnabled = false;
        if (SystemProperties.getInt("ro.logsystem.usertype", 0) == 3) {
            z = true;
        } else {
            z = false;
        }
        mbBetaUser = z;
        if (new File(Environment.getRootDirectory(), PERFTEST_FLAG_FILE).exists()) {
            mbPerfTestEnabled = true;
        } else {
            mbPerfTestEnabled = false;
        }
        if (new File(Environment.getRootDirectory(), FRAMETEST_FLAG_FILE).exists()) {
            mbFrameTestEnabled = true;
        } else {
            mbFrameTestEnabled = false;
        }
    }

    private Jlog() {
    }

    public static boolean isPerfTest() {
        return mbPerfTestEnabled;
    }

    public static boolean isFrameTest() {
        return mbFrameTestEnabled;
    }

    public static boolean isBetaUser() {
        return mbBetaUser;
    }

    public static boolean isUBMEnable() {
        return mbUBMEnable ? mbBetaUser : false;
    }

    public static boolean isEnable() {
        return true;
    }

    public static String extractAppName(String msg) {
        int nLen = msg.length();
        if (nLen <= 0 || !msg.endsWith("}")) {
            return msg;
        }
        int nStartPos = msg.indexOf(" u", 0);
        if (nStartPos < 0) {
            return msg;
        }
        nStartPos = msg.indexOf(" ", nStartPos + 2);
        if (nStartPos < 0) {
            return msg;
        }
        return msg.substring(nStartPos + 1, nLen - 1);
    }

    public static void animationStart(String msg) {
        mbEmuiAnimation = true;
        mnEmuiAnimationSkipFrames = 0;
        msEmuiAnimationSkippedFrames.delete(0, msEmuiAnimationSkippedFrames.length());
        msEmuiAnimationName = msg;
    }

    public static void animationEnd() {
        mbEmuiAnimation = false;
        mnEmuiAnimationSkipFrames = 0;
    }

    public static void animationSkipFrames(long num) {
        if (mbEmuiAnimation) {
            if (msEmuiAnimationSkippedFrames.length() == 0) {
                msEmuiAnimationSkippedFrames.append("Skipped ");
            } else {
                msEmuiAnimationSkippedFrames.append(HwCustPlusAndIddNddConvertUtils.PLUS_PREFIX);
            }
            msEmuiAnimationSkippedFrames.append(num);
            mnEmuiAnimationSkipFrames = (int) (((long) mnEmuiAnimationSkipFrames) + num);
            if (num >= 3 || mnEmuiAnimationSkipFrames >= 5) {
                d(173, msEmuiAnimationName, mnEmuiAnimationSkipFrames, msEmuiAnimationSkippedFrames.toString() + " frames");
                animationEnd();
            }
        }
    }

    public static boolean isEmuiAnimation() {
        return mbEmuiAnimation;
    }

    public static void coldLaunchingAppBegin(String pkgname) {
        if (pkgname != null) {
            msLaunchingPkgName = pkgname;
            print_janklog_native(3, 341, "#ARG1:<" + pkgname + ">");
        }
    }

    public static void warmLaunchingAppBegin(String pkgname) {
        if (pkgname != null) {
            msLaunchingPkgName = pkgname;
            print_janklog_native(3, 339, "#ARG1:<" + pkgname + ">");
        }
    }

    public static void coldLaunchingAppEnd(String pkgname) {
        if (msLaunchingPkgName != null && pkgname != null && msLaunchingPkgName.length() >= 1) {
            if (pkgname.startsWith(msLaunchingPkgName)) {
                print_janklog_native(3, 342, "#ARG1:<" + pkgname + ">");
                msLaunchingPkgName = LogException.NO_VALUE;
                return;
            }
            msLaunchingPkgName = LogException.NO_VALUE;
        }
    }

    public static void warmLaunchingAppEnd(String pkgname) {
        if (msLaunchingPkgName != null && pkgname != null && msLaunchingPkgName.length() >= 1) {
            if (pkgname.startsWith(msLaunchingPkgName)) {
                print_janklog_native(3, 340, "#ARG1:<" + pkgname + ">");
                msLaunchingPkgName = LogException.NO_VALUE;
                return;
            }
            msLaunchingPkgName = LogException.NO_VALUE;
        }
    }

    public static int d(int tag, String msg) {
        return print_janklog_native(3, tag, msg);
    }

    public static int d(int tag, String arg1, String msg) {
        return print_janklog_native(3, tag, "#ARG1:<" + arg1 + ">" + msg);
    }

    public static int d(int tag, int arg2, String msg) {
        return print_janklog_native(3, tag, "#ARG2:<" + arg2 + ">" + msg);
    }

    public static int d(int tag, String arg1, int arg2, String msg) {
        return print_janklog_native(3, tag, "#ARG1:<" + arg1 + ">#ARG2:<" + arg2 + ">" + msg);
    }

    public static int v(int tag, String msg) {
        return print_janklog_native(2, tag, msg);
    }

    public static int i(int tag, String msg) {
        return print_janklog_native(4, tag, msg);
    }

    public static int w(int tag, String msg) {
        return print_janklog_native(5, tag, msg);
    }

    public static int e(int tag, String msg) {
        return print_janklog_native(6, tag, msg);
    }

    public static boolean isHisiChipset() {
        String chipType = SystemProperties.get("ro.board.platform", LogException.NO_VALUE);
        if (chipType.startsWith("msm") || chipType.startsWith("qsc") || chipType.startsWith("MSM") || chipType.startsWith("QSC")) {
            return false;
        }
        return true;
    }
}
