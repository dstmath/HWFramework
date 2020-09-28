package android.util;

import android.content.ComponentName;
import android.content.Intent;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.IBinder;
import android.os.Process;
import android.os.SystemProperties;
import android.telephony.SmsManager;
import android.view.MotionEvent;
import com.android.internal.telephony.cdma.HwCustPlusAndIddNddConvertUtils;

public final class Jlog {
    private static final long BASE_ENABLE_MEMORY_SIZE = 3000000000L;
    public static final long CONSECU_JANK_WINDOW = 3000000000L;
    public static final int HW_LOG_ID_JANK = 6;
    public static final long INFLATE_TIME_LIMIT_NS = 16000000;
    public static final int IN_CLICK_FUNC = 1;
    public static final int IN_UNKNOWN_FUNC = 0;
    private static boolean IS_FIRST_READ_TOTAL_MEMORY = true;
    private static boolean IS_KILLED_LOG_ENABLE = false;
    public static final int JANK_UPDATE_PROP_DELAY = 900000;
    public static final int JANK_UPDATE_PROP_INTERVAL = 30000;
    public static final long MAX_VIOLATION_DURATION_MS = 8000;
    public static final long MIN_VIOLATION_DURATION_MS = 320;
    public static final long OBTAINVIEW_TIME_LIMIT_NS = 16000000;
    public static final int OPENGL_SKIPPED_FRAME_LIMIT = 30;
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
    private static final String[] RSS_LABELS = {"RssAnon:", "RssFile:", "RssShmem:"};
    public static final int SEQ_SKIPPED_FRAME_LIMIT_SINGLE = 15;
    public static final int SEQ_SKIPPED_FRAME_LIMIT_TOTAL = 90;
    public static final long SETUPVIEW_TIME_LIMIT_NS = 16000000;
    public static final int SKIPPED_FRAME_LIMIT = 5;
    private static final long VSYNC_SPAN = 16666667;
    private static boolean animStart = false;
    public static int frameCount = 0;
    private static long lastUpTime = 0;
    private static IBinder mService = null;
    private static int maxSkipFrame = 0;
    private static final boolean mbBetaUser = (SystemProperties.getInt("ro.logsystem.usertype", 0) == 3);
    private static boolean mbEmuiAnimation = false;
    private static int misPerfhubEnable = -1;
    private static int mnEmuiAnimationSkipFrames = 0;
    private static String msEmuiAnimationName;
    private static StringBuilder msEmuiAnimationSkippedFrames = new StringBuilder(32);
    private static String msLaunchingPkgName;
    private static int touchState = 0;

    public static native int print_janklog_native(int i, int i2, String str);

    private Jlog() {
    }

    private static void initKilledLogEnableState() {
        if (IS_FIRST_READ_TOTAL_MEMORY) {
            boolean z = false;
            IS_FIRST_READ_TOTAL_MEMORY = false;
            if (mbBetaUser && Process.getTotalMemory() > 3000000000L) {
                z = true;
            }
            IS_KILLED_LOG_ENABLE = z;
        }
    }

    public static void sendKilledJlog(int pid, int adj, String reason, String target) {
        initKilledLogEnableState();
        if (IS_KILLED_LOG_ENABLE) {
            long[] rssValues = {-1, -1, -1};
            if (reason.startsWith("iAwareF[LowMem")) {
                Process.readProcLines("/proc/" + pid + "/status", RSS_LABELS, rssValues);
            }
            d(396, "fwk," + pid + SmsManager.REGEX_PREFIX_DELIMITER + target + SmsManager.REGEX_PREFIX_DELIMITER + adj + SmsManager.REGEX_PREFIX_DELIMITER + rssValues[0] + "Kb," + rssValues[1] + "Kb," + rssValues[2] + "Kb," + reason);
        }
    }

    public static boolean isBetaUser() {
        return mbBetaUser;
    }

    public static boolean isEnable() {
        return true;
    }

    public static String extractAppName(String msg) {
        int nStartPos;
        int nStartPos2;
        int nLen = msg.length();
        if (nLen > 0 && msg.endsWith("}") && (nStartPos = msg.indexOf(" u", 0)) >= 0 && (nStartPos2 = msg.indexOf(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER, nStartPos + 2)) >= 0) {
            return msg.substring(nStartPos2 + 1, nLen - 1);
        }
        return msg;
    }

    public static void animationStart(String msg) {
        mbEmuiAnimation = true;
        mnEmuiAnimationSkipFrames = 0;
        StringBuilder sb = msEmuiAnimationSkippedFrames;
        sb.delete(0, sb.length());
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
                String str = msEmuiAnimationName;
                int i = mnEmuiAnimationSkipFrames;
                d(173, str, i, msEmuiAnimationSkippedFrames.toString() + " frames");
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

    public static void warmLaunchingAppBegin(String pkgname, String launchedFromPackage) {
        if (pkgname != null) {
            msLaunchingPkgName = pkgname;
            print_janklog_native(3, 339, "#ARG1:<" + pkgname + ">#FPKG:<" + launchedFromPackage + ">");
        }
    }

    public static void coldLaunchingAppEnd(String pkgname) {
        String str = msLaunchingPkgName;
        if (str != null && pkgname != null && str.length() >= 1) {
            if (!pkgname.startsWith(msLaunchingPkgName)) {
                msLaunchingPkgName = "";
                return;
            }
            print_janklog_native(3, 342, "#ARG1:<" + pkgname + ">");
            msLaunchingPkgName = "";
        }
    }

    public static void warmLaunchingAppEnd(String pkgname) {
        String str = msLaunchingPkgName;
        if (str != null && pkgname != null && str.length() >= 1) {
            if (!pkgname.startsWith(msLaunchingPkgName)) {
                msLaunchingPkgName = "";
                return;
            }
            print_janklog_native(3, 340, "#ARG1:<" + pkgname + ">");
            msLaunchingPkgName = "";
        }
    }

    public static void betaUserPrint(int tag, String msg) {
        if (mbBetaUser) {
            print_janklog_native(3, tag, msg);
        }
    }

    public static void betaUserPrint(int tag, String arg1, String msg) {
        if (mbBetaUser) {
            print_janklog_native(3, tag, "#ARG1:<" + arg1 + ">" + msg);
        }
    }

    public static void betaUserPrint(int tag, long arg1, String msg) {
        if (mbBetaUser) {
            print_janklog_native(3, tag, "#ARG1:<" + arg1 + ">" + msg);
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
        String chipType = SystemProperties.get("ro.board.platform", "");
        if (chipType.startsWith("msm") || chipType.startsWith("qsc") || chipType.startsWith("MSM") || chipType.startsWith("QSC")) {
            return false;
        }
        return true;
    }

    public static void printActivitySwitchAnimBegin() {
        d(401, "#ARG1:<App Animation Start>");
        animStart = true;
    }

    public static void printActivitySwitchAnimEnd() {
        if (animStart) {
            animStart = false;
            d(402, "#ARG1:<App Animation End>#MJ:<" + maxSkipFrame + ">");
        }
    }

    public static void recordWmsAnimJankFrame(long frameTimeNanos, long lastFrameDoneTime) {
        long skippedFrames;
        if (animStart) {
            long now = System.nanoTime();
            if (lastFrameDoneTime > frameTimeNanos) {
                skippedFrames = (now - lastFrameDoneTime) / VSYNC_SPAN;
            } else {
                skippedFrames = (now - frameTimeNanos) / VSYNC_SPAN;
            }
            if (skippedFrames > ((long) maxSkipFrame)) {
                maxSkipFrame = (int) skippedFrames;
            }
        }
    }

    public static void printStartActivityInfo(Intent intent, long start, int result) {
        if (intent != null) {
            StringBuilder sb = new StringBuilder(128);
            ComponentName componentName = intent.getComponent();
            if (componentName != null) {
                String pkg = componentName.getPackageName();
                String cls = componentName.getClassName();
                if (pkg != null) {
                    sb.append("#PKG:<");
                    sb.append(pkg);
                    sb.append(">");
                }
                if (cls != null) {
                    sb.append("#CLS:<");
                    sb.append(cls);
                    sb.append(">");
                }
            } else {
                String pkg2 = intent.getPackage();
                if (pkg2 != null) {
                    sb.append("#PKG:<");
                    sb.append(pkg2);
                    sb.append(">");
                }
                String action = intent.getAction();
                if (action != null) {
                    sb.append("#ACT:<");
                    sb.append(action);
                    sb.append(">");
                }
            }
            d(400, sb.toString() + "#ST:<" + start + ">#TS:<" + touchState + ">#UT:<" + lastUpTime + ">#RE:<" + result + ">#FC:<" + frameCount + ">");
            touchState = 0;
            lastUpTime = 0;
            frameCount = 0;
        }
    }

    public static void recordTouchState(MotionEvent motionEvent, long oldestTime) {
        int act = motionEvent.getAction();
        if (act != 2 && act == 1) {
            lastUpTime = oldestTime;
            touchState = 1;
        }
    }

    public static void setInClickFuncMark() {
        touchState = 1;
    }

    public static void clearTouchState() {
        touchState = 0;
    }
}
