package ohos.devtools;

import android.os.Process;
import android.os.SystemProperties;
import ohos.aafwk.content.Intent;
import ohos.bundle.ElementName;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public final class JLog {
    private static final long BASE_ENABLE_MEMORY_SIZE = 5000000000L;
    public static final long CONSECU_JANK_WINDOW = 3000000000L;
    public static final long INFLATE_TIME_LIMIT_NS = 16000000;
    public static final int IN_CLICK_FUNC = 1;
    public static final int IN_UNKNOWN_FUNC = 0;
    public static final int JANK_UPDATE_PROP_DELAY = 900000;
    public static final int JANK_UPDATE_PROP_INTERVAL = 30000;
    public static final HiLogLabel JLLOG_LABEL = new HiLogLabel(3, (int) JLOG_DOMAIN, "Jlog");
    public static final int JLOG_DOMAIN = 218116866;
    private static final String LIBRARY_NAME = "janklog_jni.z";
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
    public static final int ZIDANE_JANK_LOG_ID = 6;
    private static boolean animStart = false;
    private static String animationName;
    private static int animationSkipFrames = 0;
    private static StringBuilder animationSkippedFramesRecord = new StringBuilder(32);
    public static int frameCount = 0;
    private static boolean isAnimation = false;
    private static final boolean isBetaUser = (SystemProperties.getInt("ro.logsystem.usertype", 0) == 3);
    private static boolean isFirstReadTotalMemory = true;
    private static boolean isKilledLogEnable = false;
    private static boolean isLibraryLoaded;
    public static long lastUpTime = 0;
    private static String launchingBundleName;
    private static int maxSkipFrame = 0;
    public static int touchState = 0;

    public static boolean isEnable() {
        return true;
    }

    public static native int print_janklog_native(int i, int i2, String str);

    static {
        isLibraryLoaded = false;
        try {
            System.loadLibrary(LIBRARY_NAME);
            isLibraryLoaded = true;
        } catch (UnsatisfiedLinkError e) {
            HiLog.error(JLLOG_LABEL, "%{public}s is not load. %{public}s", new Object[]{LIBRARY_NAME, e.getMessage()});
            isLibraryLoaded = false;
        }
    }

    private JLog() {
    }

    private static void initKilledLogEnableState() {
        if (isFirstReadTotalMemory) {
            boolean z = false;
            isFirstReadTotalMemory = false;
            if (isBetaUser && Process.getTotalMemory() > BASE_ENABLE_MEMORY_SIZE) {
                z = true;
            }
            isKilledLogEnable = z;
        }
    }

    public static void sendKilledJlog(int i, int i2, String str, String str2) {
        initKilledLogEnableState();
        if (isKilledLogEnable) {
            long[] jArr = {-1, -1, -1};
            if (str.startsWith("iAwareF[LowMem")) {
                Process.readProcLines("/proc/" + i + "/status", RSS_LABELS, jArr);
            }
            debug(JLogConstants.JLID_PROCESS_KILLED, "fwk," + i + "," + str2 + "," + i2 + "," + jArr[0] + "Kb," + jArr[1] + "Kb," + jArr[2] + "Kb," + str);
        }
    }

    public static boolean isBetaUser() {
        return isBetaUser;
    }

    public static void animationStart(String str) {
        isAnimation = true;
        animationSkipFrames = 0;
        StringBuilder sb = animationSkippedFramesRecord;
        sb.delete(0, sb.length());
        animationName = str;
    }

    public static void animationEnd() {
        isAnimation = false;
        animationSkipFrames = 0;
    }

    public static void animationSkipFrames(long j) {
        if (isAnimation) {
            if (animationSkippedFramesRecord.length() == 0) {
                animationSkippedFramesRecord.append("Skipped ");
            } else {
                animationSkippedFramesRecord.append("+");
            }
            animationSkippedFramesRecord.append(j);
            animationSkipFrames = (int) (((long) animationSkipFrames) + j);
            if (j >= 3 || animationSkipFrames >= 5) {
                String str = animationName;
                int i = animationSkipFrames;
                debug(173, str, i, animationSkippedFramesRecord.toString() + " frames");
                animationEnd();
            }
        }
    }

    public static boolean isAnimation() {
        return isAnimation;
    }

    public static void coldLaunchingAppBegin(String str) {
        if (str != null) {
            launchingBundleName = str;
            print_janklog_native(3, JLogConstants.JLID_APP_COLDSTART_BEGIN, "#ARG1:<" + str + ">");
        }
    }

    public static void warmLaunchingAppBegin(String str, String str2) {
        if (str != null) {
            launchingBundleName = str;
            print_janklog_native(3, JLogConstants.JLID_APP_WARMSTART_BEGIN, "#ARG1:<" + str + ">#FPKG:<" + str2 + ">");
        }
    }

    public static void coldLaunchingAppEnd(String str) {
        String str2 = launchingBundleName;
        if (str2 != null && str != null && str2.length() >= 1) {
            if (!str.startsWith(launchingBundleName)) {
                launchingBundleName = "";
                return;
            }
            print_janklog_native(3, JLogConstants.JLID_APP_COLDSTART_END, "#ARG1:<" + str + ">");
            launchingBundleName = "";
        }
    }

    public static void warmLaunchingAppEnd(String str) {
        String str2 = launchingBundleName;
        if (str2 != null && str != null && str2.length() >= 1) {
            if (!str.startsWith(launchingBundleName)) {
                launchingBundleName = "";
                return;
            }
            print_janklog_native(3, JLogConstants.JLID_APP_WARMSTART_END, "#ARG1:<" + str + ">");
            launchingBundleName = "";
        }
    }

    public static void betaUserPrint(int i, String str) {
        if (isBetaUser) {
            print_janklog_native(3, i, str);
        }
    }

    public static void betaUserPrint(int i, String str, String str2) {
        if (isBetaUser) {
            print_janklog_native(3, i, "#ARG1:<" + str + ">" + str2);
        }
    }

    public static void betaUserPrint(int i, long j, String str) {
        if (isBetaUser) {
            print_janklog_native(3, i, "#ARG1:<" + j + ">" + str);
        }
    }

    public static int debug(int i, String str) {
        return print_janklog_native(3, i, str);
    }

    public static int debug(int i, String str, String str2) {
        return print_janklog_native(3, i, "#ARG1:<" + str + ">" + str2);
    }

    public static int debug(int i, int i2, String str) {
        return print_janklog_native(3, i, "#ARG2:<" + i2 + ">" + str);
    }

    public static int debug(int i, String str, int i2, String str2) {
        return print_janklog_native(3, i, "#ARG1:<" + str + ">#ARG2:<" + i2 + ">" + str2);
    }

    public static int info(int i, String str) {
        return print_janklog_native(4, i, str);
    }

    public static int warn(int i, String str) {
        return print_janklog_native(5, i, str);
    }

    public static int error(int i, String str) {
        return print_janklog_native(6, i, str);
    }

    public static int fatal(int i, String str) {
        return print_janklog_native(7, i, str);
    }

    public static void printAbilitySwitchAnimBegin() {
        debug(401, "#ARG1:<App Animation Start>");
        animStart = true;
    }

    public static void printAbilitySwitchAnimEnd() {
        if (animStart) {
            animStart = false;
            debug(JLogConstants.JLID_START_ACTIVITY_ANIMATION_END, "#ARG1:<App Animation End>#MJ:<" + maxSkipFrame + ">");
        }
    }

    public static void recordWmsAnimJankFrame(long j, long j2) {
        long j3;
        if (animStart) {
            long nanoTime = System.nanoTime();
            if (j2 > j) {
                j3 = (nanoTime - j2) / VSYNC_SPAN;
            } else {
                j3 = (nanoTime - j) / VSYNC_SPAN;
            }
            if (j3 > ((long) maxSkipFrame)) {
                maxSkipFrame = (int) j3;
            }
        }
    }

    public static void printStartAbilityInfo(Intent intent, long j, int i) {
        if (intent != null) {
            StringBuilder sb = new StringBuilder(128);
            ElementName element = intent.getElement();
            if (element != null) {
                String bundleName = element.getBundleName();
                String abilityName = element.getAbilityName();
                if (bundleName != null) {
                    sb.append("#PKG:<");
                    sb.append(bundleName);
                    sb.append(">");
                }
                if (abilityName != null) {
                    sb.append("#CLS:<");
                    sb.append(abilityName);
                    sb.append(">");
                }
            } else {
                String action = intent.getAction();
                if (action != null) {
                    sb.append("#ACT:<");
                    sb.append(action);
                    sb.append(">");
                }
            }
            debug(400, sb.toString() + "#ST:<" + j + ">#TS:<" + touchState + ">#UT:<" + lastUpTime + ">#RE:<" + i + ">#FC:<" + frameCount + ">");
            touchState = 0;
            lastUpTime = 0;
            frameCount = 0;
        }
    }

    public static void setInClickFuncMark() {
        touchState = 1;
    }

    public static void clearTouchState() {
        touchState = 0;
    }
}
