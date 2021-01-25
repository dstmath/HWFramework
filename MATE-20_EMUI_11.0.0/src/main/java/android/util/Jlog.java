package android.util;

import android.content.ComponentName;
import android.content.Intent;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Process;
import android.os.SystemProperties;
import android.telephony.SmsManager;
import android.view.MotionEvent;
import com.android.internal.telephony.cdma.HwCustPlusAndIddNddConvertUtils;

public final class Jlog {
    private static final int ACTIVITY_NAME_LENGTH = 128;
    private static final long BASE_ENABLE_MEMORY_SIZE = 3000000000L;
    private static final int BETA_USER = 3;
    public static final long CONSECU_JANK_WINDOW = 3000000000L;
    public static final int HW_LOG_ID_JANK = 6;
    public static final long INFLATE_TIME_LIMIT_NS = 16000000;
    public static final int IN_CLICK_FUNC = 1;
    public static final int IN_UNKNOWN_FUNC = 0;
    private static final boolean IS_BETA_USER = (SystemProperties.getInt("ro.logsystem.usertype", 0) == 3);
    public static final int JANK_UPDATE_PROP_DELAY = 900000;
    public static final int JANK_UPDATE_PROP_INTERVAL = 30000;
    public static final long MAX_VIOLATION_DURATION_MS = 8000;
    private static final int MIN_ANIMATION_SKIP_NUM = 3;
    private static final int MIN_JANK_SKIP_NUM = 5;
    private static final int MIN_PKG_NAME_LENGTH = 1;
    public static final long MIN_VIOLATION_DURATION_MS = 320;
    public static final long OBTAINVIEW_TIME_LIMIT_NS = 16000000;
    public static final int OPENGL_SKIPPED_FRAME_LIMIT = 30;
    private static final String[] RSS_LABELS = {"RssAnon:", "RssFile:", "RssShmem:"};
    public static final int SEQ_SKIPPED_FRAME_LIMIT_SINGLE = 15;
    public static final int SEQ_SKIPPED_FRAME_LIMIT_TOTAL = 90;
    public static final long SETUPVIEW_TIME_LIMIT_NS = 16000000;
    public static final int SKIPPED_FRAME_LIMIT = 5;
    private static final long VSYNC_SPAN = 16666667;
    private static boolean animStart = false;
    private static boolean emuiAnimation = false;
    private static String emuiAnimationName;
    private static int emuiAnimationSkipFrames = 0;
    private static StringBuilder emuiAnimationSkippedFrames = new StringBuilder(32);
    public static int frameCount = 0;
    private static boolean isFirstReadTotalMemory = true;
    private static boolean isKilledLogEnable = false;
    private static long lastUpTime = 0;
    private static String launchingPkgName;
    private static int maxSkipFrame = 0;
    private static int touchState = 0;

    public static native int print_janklog_native(int i, int i2, String str);

    private Jlog() {
    }

    private static void initKilledLogEnableState() {
        if (isFirstReadTotalMemory) {
            boolean z = false;
            isFirstReadTotalMemory = false;
            if (IS_BETA_USER && Process.getTotalMemory() > 3000000000L) {
                z = true;
            }
            isKilledLogEnable = z;
        }
    }

    public static void sendKilledJlog(int pid, int adj, String reason, String target) {
        if (reason != null && target != null) {
            initKilledLogEnableState();
            if (isKilledLogEnable) {
                long[] rssValues = {-1, -1, -1};
                if (reason.startsWith("iAwareF[LowMem")) {
                    Process.readProcLines("/proc/" + pid + "/status", RSS_LABELS, rssValues);
                }
                d(396, "fwk," + pid + SmsManager.REGEX_PREFIX_DELIMITER + target + SmsManager.REGEX_PREFIX_DELIMITER + adj + SmsManager.REGEX_PREFIX_DELIMITER + rssValues[0] + "Kb," + rssValues[1] + "Kb," + rssValues[2] + "Kb," + reason);
            }
        }
    }

    public static boolean isBetaUser() {
        return IS_BETA_USER;
    }

    public static boolean isEnable() {
        return true;
    }

    public static String extractAppName(String msg) {
        int nStartPos;
        int nStartPos2;
        if (msg == null) {
            return "";
        }
        int nLen = msg.length();
        if (nLen > 0 && msg.endsWith("}") && (nStartPos = msg.indexOf(" u", 0)) >= 0 && (nStartPos2 = msg.indexOf(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER, nStartPos + 2)) >= 0) {
            return msg.substring(nStartPos2 + 1, nLen - 1);
        }
        return msg;
    }

    public static void animationStart(String msg) {
        emuiAnimation = true;
        emuiAnimationSkipFrames = 0;
        StringBuilder sb = emuiAnimationSkippedFrames;
        sb.delete(0, sb.length());
        if (msg != null) {
            emuiAnimationName = msg;
        }
    }

    public static void animationEnd() {
        emuiAnimation = false;
        emuiAnimationSkipFrames = 0;
    }

    public static void animationSkipFrames(long num) {
        if (emuiAnimation) {
            if (emuiAnimationSkippedFrames.length() == 0) {
                emuiAnimationSkippedFrames.append("Skipped ");
            } else {
                emuiAnimationSkippedFrames.append(HwCustPlusAndIddNddConvertUtils.PLUS_PREFIX);
            }
            emuiAnimationSkippedFrames.append(num);
            emuiAnimationSkipFrames = (int) (((long) emuiAnimationSkipFrames) + num);
            if (num >= 3 || emuiAnimationSkipFrames >= 5) {
                String str = emuiAnimationName;
                int i = emuiAnimationSkipFrames;
                d(173, str, i, emuiAnimationSkippedFrames.toString() + " frames");
                animationEnd();
            }
        }
    }

    public static boolean isEmuiAnimation() {
        return emuiAnimation;
    }

    public static void coldLaunchingAppBegin(String pkgName) {
        if (pkgName != null) {
            launchingPkgName = pkgName;
            print_janklog_native(3, 341, "#ARG1:<" + pkgName + ">");
        }
    }

    public static void warmLaunchingAppBegin(String pkgName, String launchedFromPackage) {
        if (pkgName != null && launchedFromPackage != null) {
            launchingPkgName = pkgName;
            print_janklog_native(3, 339, "#ARG1:<" + pkgName + ">#FPKG:<" + launchedFromPackage + ">");
        }
    }

    public static void coldLaunchingAppEnd(String pkgName) {
        String str = launchingPkgName;
        if (str != null && pkgName != null && str.length() >= 1) {
            if (!pkgName.startsWith(launchingPkgName)) {
                launchingPkgName = "";
                return;
            }
            print_janklog_native(3, 342, "#ARG1:<" + pkgName + ">");
            launchingPkgName = "";
        }
    }

    public static void warmLaunchingAppEnd(String pkgName) {
        String str = launchingPkgName;
        if (str != null && pkgName != null && str.length() >= 1) {
            if (!pkgName.startsWith(launchingPkgName)) {
                launchingPkgName = "";
                return;
            }
            print_janklog_native(3, 340, "#ARG1:<" + pkgName + ">");
            launchingPkgName = "";
        }
    }

    public static void betaUserPrint(int tag, String msg) {
        if (msg != null && IS_BETA_USER) {
            print_janklog_native(3, tag, msg);
        }
    }

    public static void betaUserPrint(int tag, String arg1, String msg) {
        if (msg != null && IS_BETA_USER) {
            print_janklog_native(3, tag, "#ARG1:<" + arg1 + ">" + msg);
        }
    }

    public static void betaUserPrint(int tag, long arg1, String msg) {
        if (msg != null && IS_BETA_USER) {
            print_janklog_native(3, tag, "#ARG1:<" + arg1 + ">" + msg);
        }
    }

    public static int d(int tag, String msg) {
        if (msg == null) {
            return -1;
        }
        return print_janklog_native(3, tag, msg);
    }

    public static int d(int tag, String arg1, String msg) {
        if (msg == null) {
            return -1;
        }
        return print_janklog_native(3, tag, "#ARG1:<" + arg1 + ">" + msg);
    }

    public static int d(int tag, int arg2, String msg) {
        if (msg == null) {
            return -1;
        }
        return print_janklog_native(3, tag, "#ARG2:<" + arg2 + ">" + msg);
    }

    public static int d(int tag, String arg1, int arg2, String msg) {
        if (arg1 == null || msg == null) {
            return -1;
        }
        return print_janklog_native(3, tag, "#ARG1:<" + arg1 + ">#ARG2:<" + arg2 + ">" + msg);
    }

    public static int v(int tag, String msg) {
        if (msg == null) {
            return -1;
        }
        return print_janklog_native(2, tag, msg);
    }

    public static int i(int tag, String msg) {
        if (msg == null) {
            return -1;
        }
        return print_janklog_native(4, tag, msg);
    }

    public static int w(int tag, String msg) {
        if (msg == null) {
            return -1;
        }
        return print_janklog_native(5, tag, msg);
    }

    public static int e(int tag, String msg) {
        if (msg == null) {
            return -1;
        }
        return print_janklog_native(6, tag, msg);
    }

    public static boolean isHisiChipset() {
        String chipType = SystemProperties.get("ro.board.platform", "");
        if (chipType != null && !chipType.startsWith("msm") && !chipType.startsWith("qsc") && !chipType.startsWith("MSM") && !chipType.startsWith("QSC")) {
            return true;
        }
        return false;
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
        if (motionEvent != null && motionEvent.getAction() == 1) {
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
